/*
 * Copyright ©2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rockscript.engine;

import java.util.*;
import java.util.stream.Collectors;

import io.rockscript.ServiceLocator;
import io.rockscript.gson.PolymorphicTypeAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventStore implements EventListener {

  static Logger log = LoggerFactory.getLogger(EventStore.class);

  static Gson gson = new GsonBuilder()
    .registerTypeAdapterFactory(new PolymorphicTypeAdapterFactory()
      .typeName(new TypeToken<StartScriptEventJson>(){},              "startScript")
      .typeName(new TypeToken<EndScriptEventJson>(){},                "endScript")
      .typeName(new TypeToken<StartExecutionEventJson>(){},           "startExecution")
      .typeName(new TypeToken<VariableCreatedEventJson>(){},          "variableCreated")
      .typeName(new TypeToken<ImportFunctionEventJson>(){},           "importInvocation")
      .typeName(new TypeToken<ActionStartEventJson>(){},              "actionStart")
      .typeName(new TypeToken<ActionWaitEventJson>(){},               "actionWait")
      .typeName(new TypeToken<ActionEndedEventJson>(){},              "actionEnd")
      .typeName(new TypeToken<IdentifierExpressionEventJson>(){},     "variableExpression")
      .typeName(new TypeToken<MemberDotExpressionEventJson>(){},      "memberDot")
      .typeName(new TypeToken<ObjectLiteralExpressionEventJson>(){},  "objectLiteralExpression")
    )

    // .setPrettyPrinting()
    .create();

  ServiceLocator serviceLocator;

  public EventStore(ServiceLocator serviceLocator) {
    this.serviceLocator = serviceLocator;
  }

  List<EventJson> events = new ArrayList<>();

  @Override
  public void handle(Event event) {
    EventJson gsonnable = event.toJson();
    events.add(gsonnable);
    String jsonString = gson.toJson(gsonnable);
    log.debug(jsonString);
  }

  public List<EventJson> findEventsByScriptExecutionId(String scriptExecutionId) {
    return events.stream()
      .filter(event->scriptExecutionId.equals(event.getScriptExecutionId()))
      .collect(Collectors.toList());
  }

  public ScriptExecution loadScriptExecution(String scriptExecutionId) {
    List<EventJson> eventJsons = findEventsByScriptExecutionId(scriptExecutionId);

    return recreateScriptExecution(eventJsons, scriptExecutionId);
  }

  private ScriptExecution recreateScriptExecution(List<EventJson> eventJsons, String scriptExecutionId) {
    String scriptId = findScriptId(eventJsons);
    ScriptException.throwIfNull(scriptId, "Script id not found for scriptExecutionId: %s", scriptExecutionId);
    Script script = serviceLocator
      .getScriptStore()
      .loadScript(scriptId);
    ScriptException.throwIfNull(scriptId, "Script not found for scriptId: %s", scriptId);

    ScriptExecution scriptExecution = new ScriptExecution(scriptExecutionId, serviceLocator, script);
    scriptExecution.executionMode = ExecutionMode.REBUILDING;

    for (EventJson eventJson: eventJsons) {
      if (isExecutable(eventJson)) {
        Execution execution = scriptExecution.findExecutionRecursive(eventJson.executionId);
        ExecutableEvent event = (ExecutableEvent) eventJson.toEvent(execution);
        log.debug("reexecuting: "+gson.toJson(eventJson));
        event.execute();
      } else {
        log.debug("skipping...: "+gson.toJson(eventJson));
      }
    }

    scriptExecution.executionMode = ExecutionMode.EXECUTING;

    return scriptExecution;
  }

  private String findScriptId(List<EventJson> scriptExecutionEventJsons) {
    return scriptExecutionEventJsons.stream()
                                    .map(eventJson->eventJson.getScriptId())
                                    .filter(Objects::nonNull)
                                    .findFirst()
                                    .get();
  }

  public List<ScriptExecution> recoverCrashedScriptExecutions() {
    List<ScriptExecution> scriptExecutions = new ArrayList<>();
    Map<String,List<EventJson>> groupedEvents = findCrashedScriptExecutionEvents();
    for (String scriptExecutionId: groupedEvents.keySet()) {
      List<EventJson> scriptExecutionEvents = groupedEvents.get(scriptExecutionId);
      while (!lastEventIsProceedable(scriptExecutionEvents)) {
        scriptExecutionEvents.remove(scriptExecutionEvents.size()-1);
      }
      ScriptExecution scriptExecution = recreateScriptExecution(scriptExecutionEvents, scriptExecutionId);
      EventJson recoverableEventJson = scriptExecutionEvents.get(scriptExecutionEvents.size()-1);
      Execution execution = scriptExecution.findExecutionRecursive(recoverableEventJson.getExecutionId());
      ExecutableEvent executableEvent = (ExecutableEvent) recoverableEventJson.toEvent(execution);
      executableEvent.execute();
      scriptExecutions.add(scriptExecution);
    }
    return scriptExecutions;
  }

  private boolean lastEventIsProceedable(List<EventJson> scriptExecutionEvents) {
    EventJson lastEvent = scriptExecutionEvents.get(scriptExecutionEvents.size()-1);
    return RecoverableEventJson.class.isAssignableFrom(lastEvent.getClass());
  }

  /** @return a list of events grouped by script execution. */
  public Map<String,List<EventJson>> findCrashedScriptExecutionEvents() {
    Map<String,List<EventJson>> groupedEvents = new HashMap<>();
    for (EventJson event: events) {
      String scriptExecutionId = event.getScriptExecutionId();
      if (event instanceof EndScriptEventJson) {
        groupedEvents.remove(scriptExecutionId);
      } else {
        List<EventJson> scriptExecutionEvents = groupedEvents.get(scriptExecutionId);
        if (scriptExecutionEvents==null) {
          scriptExecutionEvents = new ArrayList<>();
          groupedEvents.put(scriptExecutionId, scriptExecutionEvents);
        }
        scriptExecutionEvents.add(event);
      }
    }
    for (String scriptExecutionId: new ArrayList<>(groupedEvents.keySet())) {
      List<EventJson> scriptExecutionEvents = groupedEvents.get(scriptExecutionId);
      EventJson lastEventJson = scriptExecutionEvents.get(scriptExecutionEvents.size()-1);
      if (isUnlocking(lastEventJson)) {
        groupedEvents.remove(scriptExecutionId);
      }
    }
    return groupedEvents;
  }

  private boolean isExecutable(EventJson eventJson) {
    return eventJson!=null
      && ( eventJson instanceof StartScriptEventJson
           || eventJson instanceof ActionEndedEventJson);
  }

  private boolean isUnlocking(EventJson lastEventJson) {
    return lastEventJson!=null
      && ( lastEventJson instanceof ActionWaitEventJson
           || lastEventJson instanceof EndScriptEventJson);
  }

  public String toJson(Event event) {
    return event!=null ? gson.toJson(event.toJson()) : "null";
  }
}

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.rockscript.ServiceLocator;
import io.rockscript.gson.PolymorphicTypeAdapterFactory;
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

  private static class LoadingWrapperEventListener implements EventListener {
    ScriptExecution scriptExecution;
    EventListener originalEventListener;
    int previouslyExecutedEvents;
    int replayedEvents;

    public LoadingWrapperEventListener(ScriptExecution scriptExecution, EventListener originalEventListener, List<EventJson> eventJsons) {
      this.scriptExecution = scriptExecution;
      this.originalEventListener = originalEventListener;
      this.previouslyExecutedEvents = eventJsons.size();
      this.replayedEvents = 0;
    }

    @Override
    public void handle(Event event) {
      updateExecutionModeAndCount(event);
      if (scriptExecution.getExecutionMode()==ExecutionMode.EXECUTING) {
        originalEventListener.handle(event);
      } else {
        log.debug("Swallowing ("+scriptExecution.getExecutionMode()+"): "+gson.toJson(event.toJson()));
      }
    }

    private void updateExecutionModeAndCount(Event event) {
      if (replayedEvents==0) {
        if (previouslyExecutedEvents==1) {
          scriptExecution.setExecutionMode(ExecutionMode.RECOVERING);
        } else {
          scriptExecution.setExecutionMode(ExecutionMode.REBUILDING);
        }
      } else if (scriptExecution.getExecutionMode()==ExecutionMode.REBUILDING) {
        if (replayedEvents==previouslyExecutedEvents-1) {
          scriptExecution.setExecutionMode(ExecutionMode.RECOVERING);
        }
      } else if (scriptExecution.getExecutionMode()==ExecutionMode.RECOVERING) {
        scriptExecution.setExecutionMode(ExecutionMode.EXECUTING);
      }
      replayedEvents++;
    }

    public void eventExecuting(Event event) {
      updateExecutionModeAndCount(event);
    }
  }

  private ScriptExecution recreateScriptExecution(List<EventJson> eventJsons, String scriptExecutionId) {
    String scriptId = findScriptId(eventJsons);
    ScriptException.throwIfNull(scriptId, "Script id not found for scriptExecutionId: %s", scriptExecutionId);
    Script script = serviceLocator
      .getScriptStore()
      .loadScript(scriptId);
    ScriptException.throwIfNull(scriptId, "Script not found for scriptId: %s", scriptId);

    ScriptExecution scriptExecution = new ScriptExecution(scriptExecutionId, serviceLocator, script);

    EventListener originalEventListener = scriptExecution.getEventListener();
    LoadingWrapperEventListener loadingWrapperEventListener = new LoadingWrapperEventListener(scriptExecution, originalEventListener, eventJsons);
    scriptExecution.setEventListener(loadingWrapperEventListener);

    for (EventJson eventJson: eventJsons) {
      if (isExecutable(eventJson)) {
        if (scriptExecution.getExecutionMode()==ExecutionMode.RECOVERING) {
          scriptExecution.setExecutionMode(ExecutionMode.EXECUTING);
        }
        Execution execution = scriptExecution.findExecutionRecursive(eventJson.executionId);
        ExecutableEvent event = (ExecutableEvent) eventJson.toEvent(execution);

        // The events that are being executed are not dispatched and hence the
        // LoadingWrapperEventListener doesn't receive them, yet it also must keep
        // track of those to get the counting right
        loadingWrapperEventListener.eventExecuting(event);
        log.debug("Executing ("+scriptExecution.getExecutionMode()+"): "+gson.toJson(eventJson));
        event.execute();
      }
    }

    scriptExecution.setExecutionMode(ExecutionMode.EXECUTING);

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
      ScriptExecution scriptExecution = recreateScriptExecution(scriptExecutionEvents, scriptExecutionId);
      scriptExecutions.add(scriptExecution);
    }
    return scriptExecutions;
  }

  private boolean lastEventIsExecutable(List<EventJson> scriptExecutionEvents) {
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

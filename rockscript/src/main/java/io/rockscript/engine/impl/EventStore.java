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

package io.rockscript.engine.impl;

import io.rockscript.Engine;
import io.rockscript.engine.EngineException;
import io.rockscript.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class EventStore implements EventListener {

  static final Logger log = LoggerFactory.getLogger(EventStore.class);

  Engine engine;
  List<Event> events = new ArrayList<>();

  public EventStore(Engine engine) {
    this.engine = engine;
  }

  @Override
  public void handle(Event event) {
    events.add(event);
  }

  public List<ExecutionEvent> findEventsByScriptExecutionId(String scriptExecutionId) {
    return events.stream()
      .filter(event-> event instanceof ExecutionEvent)
      .map(event->((ExecutionEvent)event))
      .filter(executionEvent->scriptExecutionId.equals(executionEvent.getScriptExecutionId()))
      .collect(Collectors.toList());
  }

  public EngineScriptExecution findScriptExecutionById(String scriptExecutionId) {
    List<ExecutionEvent> executionEvents = findEventsByScriptExecutionId(scriptExecutionId);
    return recreateScriptExecution(executionEvents, scriptExecutionId);
  }

  public List<Event> getEvents() {
    return events;
  }

  private EngineScriptExecution recreateScriptExecution(List<ExecutionEvent> executionEvents, String scriptExecutionId) {
    if (executionEvents==null || executionEvents.isEmpty()) {
      throw new EngineException("ScriptVersion execution "+scriptExecutionId+" does not exist");
    }

    ScriptStartedEvent scriptStartedEvent = findScriptStartedEventJson(executionEvents);
    if (scriptStartedEvent==null) {
      throw new EngineException("ScriptVersion execution "+scriptExecutionId+" does not have a start event. Huh?!");
    }

    String scriptId = scriptStartedEvent.getScriptVersionId();
    EngineException.throwIfNull(scriptId, "EngineScript id is null in scriptStartedEvent for engineScript execution: %s", scriptExecutionId);
    EngineScript engineScript = engine
      .getScriptStore()
      .findScriptAstByScriptVersionId(scriptId);
    EngineException.throwIfNull(scriptId, "EngineScript not found for scriptId %s in engineScript execution %s", scriptId, scriptExecutionId);

    EngineScriptExecution scriptExecution = new EngineScriptExecution(scriptExecutionId, engine, engineScript, executionEvents);
    scriptExecution.doWork();

    return scriptExecution;
  }

  private ScriptStartedEvent findScriptStartedEventJson(List<ExecutionEvent> scriptExecutionEvents) {
    // Normally the ScriptStartedEventJson should be the first in the list so this should be quick
    return (ScriptStartedEvent) scriptExecutionEvents.stream()
      .filter(event->(event instanceof ScriptStartedEvent))
      .findFirst()
      .orElse(null);
  }

  public List<EngineScriptExecution> recoverCrashedScriptExecutions() {
    List<EngineScriptExecution> scriptExecutions = new ArrayList<>();
    Map<String,List<ExecutionEvent>> groupedEvents = findCrashedScriptExecutionEvents();
    for (String scriptExecutionId: groupedEvents.keySet()) {
      List<ExecutionEvent> scriptExecutionEvents = groupedEvents.get(scriptExecutionId);
      EngineScriptExecution scriptExecution = recreateScriptExecution(scriptExecutionEvents, scriptExecutionId);
      scriptExecutions.add(scriptExecution);
    }
    return scriptExecutions;
  }

  /** @return a list of events grouped by engineScript execution. */
  public Map<String,List<ExecutionEvent>> findCrashedScriptExecutionEvents() {
    Map<String,List<ExecutionEvent>> groupedEvents = new HashMap<>();
    for (Event event: events) {
      if (event instanceof ExecutionEvent) {
        ExecutionEvent executableEvent = (ExecutionEvent) event;
        String scriptExecutionId = executableEvent.getScriptExecutionId();
        if (executableEvent instanceof ScriptEndedEvent) {
          groupedEvents.remove(scriptExecutionId);
        } else {
          List<ExecutionEvent> scriptExecutionEvents = groupedEvents.get(scriptExecutionId);
          if (scriptExecutionEvents==null) {
            scriptExecutionEvents = new ArrayList<>();
            groupedEvents.put(scriptExecutionId, scriptExecutionEvents);
          }
          scriptExecutionEvents.add(executableEvent);
        }
      }
    }
    for (String scriptExecutionId: new ArrayList<>(groupedEvents.keySet())) {
      List<ExecutionEvent> scriptExecutionEvents = groupedEvents.get(scriptExecutionId);
      ExecutionEvent lastEvent = scriptExecutionEvents.get(scriptExecutionEvents.size()-1);
      if (isUnlocking(lastEvent)) {
        groupedEvents.remove(scriptExecutionId);
      }
    }
    return groupedEvents;
  }

  private boolean isUnlocking(ExecutionEvent lastEvent) {
    return lastEvent!=null
      && ( lastEvent instanceof ActivityWaitingEvent
           || lastEvent instanceof ScriptEndedEvent);
  }

  public Object valueToJson(Object value) {
    if (value==null) {
      return "null";
    }
    if (value instanceof Activity) {
      return value.toString();
    }
    if (value instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String,Object> map = (Map<String,Object>) value;
      return valueMapToJson(map);
    }
    return value;
  }

  private Map<String,Object> valueMapToJson(Map<String,Object> map) {
    Map<String,Object> convertedMap = new LinkedHashMap<>();
    for (String key: map.keySet()) {
      Object convertedValue = valueToJson(map.get(key));
      convertedMap.put(key, convertedValue);
    }
    return convertedMap;
  }

  public boolean hasScriptExecution(String scriptExecutionId) {
    return events.stream()
      .filter(event-> event instanceof ExecutionEvent)
      .map(event->((ExecutionEvent)event))
      .filter(executionEvent->scriptExecutionId.equals(executionEvent.getScriptExecutionId()))
      .findFirst()
      .isPresent();
  }
}

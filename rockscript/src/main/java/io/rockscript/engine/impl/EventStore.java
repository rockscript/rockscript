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
import io.rockscript.api.events.*;
import io.rockscript.engine.EngineException;
import io.rockscript.service.ServiceFunction;
import io.rockscript.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static io.rockscript.util.Lists.getLast;
import static io.rockscript.util.Lists.setLast;

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

  public EngineScriptExecution findScriptExecutionById(String scriptExecutionId) {
    List<ExecutionEvent> executionEvents = findEventsByScriptExecutionId(scriptExecutionId);
    return replayScriptExecution(executionEvents, scriptExecutionId, false);
  }

  public List<ExecutionEvent> findEventsByScriptExecutionId(String scriptExecutionId) {
    return events.stream()
      .filter(event-> event instanceof ExecutionEvent)
      .map(event->((ExecutionEvent)event))
      .filter(executionEvent->scriptExecutionId.equals(executionEvent.getScriptExecutionId()))
      .collect(Collectors.toList());
  }

  private boolean isLastEventUnlocking(List<ExecutionEvent> executionEvents) {
    return executionEvents.get(executionEvents.size()-1).isUnlocking();
  }

  /** When recovering, we remove all the 'dangling' events at the end
   * until the last event is a recoverable event.  Recoverable means a
   * startScript or a serviceFunctionStartEvent. */
  private void removeEventsThatWillBeReplayed(List<ExecutionEvent> executionEvents) {
    while (!getLast(executionEvents).isRecoverable()) {
      executionEvents.remove(executionEvents.size() - 1);
    }
  }

  private void throwExceptionInconsistentEventStream(List<ExecutionEvent> executionEvents) {
    String eventsText = executionEvents.stream()
      .map(event->event.toString())
      .collect(Collectors.joining("\n"));
    throw new RuntimeException("Inconsistent event stream. This script execution needs recovery:\n"+eventsText);
  }


  private boolean isLastRecoverable(List<ExecutionEvent> executionEvents) {
    Class<? extends ExecutionEvent> eventClass = executionEvents.get(executionEvents.size() - 1).getClass();
    return eventClass==ScriptStartedEvent.class || eventClass==ServiceFunctionStartedEvent.class;
  }

  private EngineScriptExecution replayScriptExecution(List<ExecutionEvent> executionEvents, String scriptExecutionId, boolean recovering) {
    if (executionEvents==null || executionEvents.isEmpty()) {
      throw new RuntimeException("Inconsistent event stream. No events.");
    }

    // If the event sequence was not properly ended with a locking event,
    if (!isLastEventUnlocking(executionEvents)) {
      // If we want to recover this execution
      if (recovering) {
        removeEventsThatWillBeReplayed(executionEvents);
      } else {
        throwExceptionInconsistentEventStream(executionEvents);
      }
    }

    // Remove all non replay events and return
    // the result as executable events
    List<ExecutableEvent> replayEvents = executionEvents.stream()
      .filter(executionEvent->executionEvent.isReplay())
      .map(executionEvent->(ExecutableEvent)executionEvent)
      .collect(Collectors.toList());

    if (replayEvents==null || replayEvents.isEmpty()) {
      throw new EngineException("Script execution "+scriptExecutionId+" does not exist");
    }

    ScriptStartedEvent scriptStartedEvent = findScriptStartedEventJson(replayEvents);
    if (scriptStartedEvent==null) {
      throw new EngineException("Script execution "+scriptExecutionId+" does not have a start event. Huh?!");
    }

    String scriptId = scriptStartedEvent.getScriptVersionId();
    EngineException.throwIfNull(scriptId, "Script id is null in scriptStartedEvent for engineScript execution: %s", scriptExecutionId);
    EngineScript engineScript = engine
      .getScriptStore()
      .findScriptAstByScriptVersionId(scriptId);
    EngineException.throwIfNull(scriptId, "Script not found for scriptId %s in engineScript execution %s", scriptId, scriptExecutionId);

    EngineScriptExecution scriptExecution = new EngineScriptExecution(scriptExecutionId, engine, engineScript);
    scriptExecution.setExecutionMode(ExecutionMode.REPLAYING);

    log.info("Replaying script execution from events:");
    replayEvents.forEach(replayEvent->{
      String executionId = replayEvent.getExecutionId();
      // Script execution events do not have an executionId in the event, only the scriptExecutionId.
      Execution execution = executionId!=null ? scriptExecution.findExecutionRecursive(executionId) : scriptExecution;

      if (recovering && replayEvent==getLast(replayEvents)) {
        scriptExecution.setExecutionMode(ExecutionMode.RECOVERING);
      }

      log.info("Reexecuting event: "+replayEvent);
      replayEvent.execute(execution);
    });

    scriptExecution.setExecutionMode(ExecutionMode.EXECUTING);
    scriptExecution.doWork();

    return scriptExecution;
  }

  private ScriptStartedEvent findScriptStartedEventJson(List<? extends ExecutionEvent> scriptExecutionEvents) {
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
      List<ExecutionEvent> executionEvents = groupedEvents.get(scriptExecutionId);
      EngineScriptExecution scriptExecution = replayScriptExecution(executionEvents, scriptExecutionId, true);
      scriptExecutions.add(scriptExecution);
    }
    return scriptExecutions;
  }

  /** @return a list of events grouped by engineScript execution. */
  public Map<String,List<ExecutionEvent>> findCrashedScriptExecutionEvents() {

    // TODO only scan for the script executions that have an expired lock

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
      if (lastEvent.isUnlocking()) {
        groupedEvents.remove(scriptExecutionId);
      }
    }
    return groupedEvents;
  }

  public Object valueToJson(Object value) {
    if (value==null) {
      return "null";
    }
    if (value instanceof ServiceFunction) {
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

  public List<Event> getEvents() {
    return events;
  }
}

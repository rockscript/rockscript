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

import io.rockscript.ScriptException;
import io.rockscript.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class EventStore implements EventListener {

  static final Logger log = LoggerFactory.getLogger(EventStore.class);
  static final Logger eventLog = LoggerFactory.getLogger(EventStore.class.getName()+".events");

  EngineConfiguration engineConfiguration;
  List<Event> events = new ArrayList<>();

  public EventStore(EngineConfiguration engineConfiguration) {
    this.engineConfiguration = engineConfiguration;
  }

  @Override
  public void handle(Event event) {
    events.add(event);
    String jsonString = eventJsonToJsonString(event);
    eventLog.debug(jsonString);
  }

  public String eventJsonToJsonString(Event event) {
    return event!=null ? engineConfiguration.getGson().toJson(event) : "null";
  }

  public List<ExecutionEvent> findEventsByScriptExecutionId(String scriptExecutionId) {
    return events.stream()
      .filter(event-> event instanceof ExecutionEvent)
      .map(event->((ExecutionEvent)event))
      .filter(executionEvent->scriptExecutionId.equals(executionEvent.getScriptExecutionId()))
      .collect(Collectors.toList());
  }

  public ScriptExecution findScriptExecutionById(String scriptExecutionId) {
    List<ExecutionEvent> executionEvents = findEventsByScriptExecutionId(scriptExecutionId);
    return recreateScriptExecution(executionEvents, scriptExecutionId);
  }

  public List<Event> getEvents() {
    return events;
  }

  private class LoadingWrapperEventListener implements EventListener {
    ScriptExecution scriptExecution;
    EventListener originalEventListener;
    int previouslyExecutedEvents;
    int replayedEvents;

    public LoadingWrapperEventListener(ScriptExecution scriptExecution, EventListener originalEventListener, List<ExecutionEvent> executionEvents) {
      this.scriptExecution = scriptExecution;
      this.originalEventListener = originalEventListener;
      this.previouslyExecutedEvents = executionEvents.size()+1;
      this.replayedEvents = 0;
    }

    @Override
    public void handle(Event event) {
      if (event instanceof ExecutionEvent) {
        ExecutionEvent executionEvent = (ExecutionEvent) event;
        incrementEventCount();
        if (scriptExecution.getExecutionMode()==ExecutionMode.EXECUTING) {
          originalEventListener.handle(event);
        }
//        else {
//          log.debug("Swallowing ("+scriptExecution.getExecutionMode()+"): "+ EventStore.this.eventToJsonString(executionEvent));
//        }
      }
    }

    private void incrementEventCount() {
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

    public void eventExecuting(ExecutionEvent event) {
      incrementEventCount();
    }
  }

  private ScriptExecution recreateScriptExecution(List<ExecutionEvent> executionEvents, String scriptExecutionId) {
    ScriptStartedEvent scriptStartedEvent = findScriptStartedEventJson(executionEvents);

    String scriptId = scriptStartedEvent.getScriptId();
    ScriptException.throwIfNull(scriptId, "ScriptAst id is null in scriptStartedEvent for scriptAst execution: %s", scriptExecutionId);
    ScriptAst scriptAst = engineConfiguration
      .getScriptStore()
      .findScriptAstById(scriptId);
    ScriptException.throwIfNull(scriptId, "ScriptAst not found for scriptId %s in scriptAst execution %s", scriptId, scriptExecutionId);
    Object inputJson = scriptStartedEvent.getInput();
    // For now, the input json is not deserialized
    // Later we might add special deserialization to handle activities and functions etc
    Object input = inputJson;
    ScriptExecution scriptExecution = new ScriptExecution(scriptExecutionId, engineConfiguration, scriptAst);
    scriptExecution.setInput(input);

    EventListener originalEventListener = scriptExecution.getEventListener();
    LoadingWrapperEventListener loadingWrapperEventListener = new LoadingWrapperEventListener(scriptExecution, originalEventListener, executionEvents);
    scriptExecution.setEventListener(loadingWrapperEventListener);
    // This is for the StartScriptEvent which already has been processed
    loadingWrapperEventListener.incrementEventCount();

    scriptExecution.start();

    List<ExecutableEvent> executableEvents = executionEvents.stream()
      .filter(this::isExecutable)
      .map(executionEvent-> (ExecutableEvent)executionEvent)
      .collect(Collectors.toList());

    for (ExecutableEvent event: executableEvents) {
      Execution execution = scriptExecution.findExecutionRecursive(event.executionId);
      // The events that are being executed are not dispatched and hence the
      // LoadingWrapperEventListener doesn't receive them, yet it also must keep
      // track of those to get the counting right
      loadingWrapperEventListener.eventExecuting(event);
      // log.debug("Executing ("+scriptExecution.getExecutionMode()+"): "+eventJsonToJsonString(event));
      event.execute(execution);
    }

    scriptExecution.setExecutionMode(ExecutionMode.EXECUTING);

    return scriptExecution;
  }

  private ScriptStartedEvent findScriptStartedEventJson(List<ExecutionEvent> scriptExecutionEvents) {
    // Normally the ScriptStartedEventJson should be the first in the list so this should be quick
    return (ScriptStartedEvent) scriptExecutionEvents.stream()
      .filter(event->(event instanceof ScriptStartedEvent))
      .findFirst()
      .get();
  }

  public List<ScriptExecution> recoverCrashedScriptExecutions() {
    List<ScriptExecution> scriptExecutions = new ArrayList<>();
    Map<String,List<ExecutionEvent>> groupedEvents = findCrashedScriptExecutionEvents();
    for (String scriptExecutionId: groupedEvents.keySet()) {
      List<ExecutionEvent> scriptExecutionEvents = groupedEvents.get(scriptExecutionId);
      ScriptExecution scriptExecution = recreateScriptExecution(scriptExecutionEvents, scriptExecutionId);
      scriptExecutions.add(scriptExecution);
    }
    return scriptExecutions;
  }

  /** @return a list of events grouped by scriptAst execution. */
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

  private boolean isExecutable(ExecutionEvent event) {
    return (event instanceof ExecutableEvent);
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
    if (value instanceof JsonObject) {
      return valueMapToJson(((JsonObject)value).properties);
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
}

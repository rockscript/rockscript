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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import io.muoncore.Muon;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.client.DefaultEventClient;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventReplayMode;
import io.rockscript.action.Action;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//DD, with the Muon addition, this becomes an event facade on top of a Muon event stack based event store somewhere.
public class EventStore implements EventListener {

  static final Logger log = LoggerFactory.getLogger(EventStore.class);
  static final Logger eventLog = LoggerFactory.getLogger(EventStore.class.getName()+".events");

  EngineConfiguration engineConfiguration;
  private Muon muon;
  private EventClient evClient;

  public EventStore(EngineConfiguration engineConfiguration, Muon muon) {
    this.engineConfiguration = engineConfiguration;
    this.muon = muon;
    evClient = new DefaultEventClient(muon);
  }

  @Override
  public void handle(Event event) {
    EventJson gsonnable = event.toJson();
    String jsonString = eventJsonToJsonString(gsonnable);
    eventLog.debug(jsonString);

    emitCentralStreamEvent(gsonnable);
    emitEventSourceEvent(gsonnable);
  }

  private void emitCentralStreamEvent(EventJson gsonnable) {
    evClient.event(
            ClientEvent
                    .ofType(gsonnable.getClass().getCanonicalName())
                    .stream(getRockscriptStream())
                    .payload(gsonnable).build());
  }

  private String getRockscriptStream() {
    return "rockscript";
  }

  private void emitEventSourceEvent(EventJson gsonnable) {
    if (ExecutionEventJson.class.isAssignableFrom(gsonnable.getClass())) {
      String executionId = ((ExecutionEventJson) gsonnable).getScriptExecutionId();

      evClient.event(
              ClientEvent
                      .ofType(gsonnable.getClass().getCanonicalName())
                      .stream(getExecutionStreamName(executionId))
                      .payload(gsonnable).build());
    }
  }

  private String getExecutionStreamName(String executionId) {
    return "rockscript_execution/" + executionId;
  }

  public List<ExecutionEventJson> findEventsByScriptExecutionId(String scriptExecutionId) {

    return coldReplayEvents(getExecutionStreamName(scriptExecutionId),
            ExecutionEventJson.class);
  }

  public ScriptExecution findScriptExecutionById(String scriptExecutionId) {
    List<ExecutionEventJson> eventJsons = findEventsByScriptExecutionId(scriptExecutionId);
    return recreateScriptExecution(eventJsons, scriptExecutionId);
  }

  public List<EventJson> getEvents() {
    return coldReplayEvents(getRockscriptStream(), EventJson.class);
  }

  private class LoadingWrapperEventListener implements EventListener {
    ScriptExecution scriptExecution;
    EventListener originalEventListener;
    int previouslyExecutedEvents;
    int replayedEvents;

    public LoadingWrapperEventListener(ScriptExecution scriptExecution, EventListener originalEventListener, List<ExecutionEventJson> eventJsons) {
      this.scriptExecution = scriptExecution;
      this.originalEventListener = originalEventListener;
      this.previouslyExecutedEvents = eventJsons.size()+1;
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

  private ScriptExecution recreateScriptExecution(List<ExecutionEventJson> eventJsons, String scriptExecutionId) {
    ScriptStartedEventJson scriptStartedEventJson = findScriptStartedEventJson(eventJsons);

    String scriptId = scriptStartedEventJson.getScriptId();
    ScriptException.throwIfNull(scriptId, "Script id is null in scriptStartedEvent for script execution: %s", scriptExecutionId);
    Script script = engineConfiguration
      .getScriptStore()
      .findScriptById(scriptId);
    ScriptException.throwIfNull(scriptId, "Script not found for scriptId %s in script execution %s", scriptId, scriptExecutionId);
    Object inputJson = scriptStartedEventJson.getInput();
    // For now, the input json is not deserialized
    // Later we might add special deserialization to handle actions and functionsetc
    Object input = inputJson;
    ScriptExecution scriptExecution = new ScriptExecution(scriptExecutionId, engineConfiguration, script);
    scriptExecution.setInput(input);

    EventListener originalEventListener = scriptExecution.getEventListener();
    LoadingWrapperEventListener loadingWrapperEventListener = new LoadingWrapperEventListener(scriptExecution, originalEventListener, eventJsons);
    scriptExecution.setEventListener(loadingWrapperEventListener);
    // This is for the StartScriptEvent which already has been processed
    loadingWrapperEventListener.incrementEventCount();

    scriptExecution.start();

    eventJsons = eventJsons.stream()
      .filter(this::isExecutable)
      .collect(Collectors.toList());

    for (ExecutionEventJson eventJson: eventJsons) {
      Execution execution = scriptExecution.findExecutionRecursive(eventJson.executionId);
      ExecutableEvent event = (ExecutableEvent) eventJson.toEvent(execution);

      // The events that are being executed are not dispatched and hence the
      // LoadingWrapperEventListener doesn't receive them, yet it also must keep
      // track of those to get the counting right
      loadingWrapperEventListener.eventExecuting(event);
      // log.debug("Executing ("+scriptExecution.getExecutionMode()+"): "+eventJsonToJsonString(eventJson));
      event.execute();
    }

    scriptExecution.setExecutionMode(ExecutionMode.EXECUTING);

    return scriptExecution;
  }

  private ScriptStartedEventJson findScriptStartedEventJson(List<ExecutionEventJson> scriptExecutionEventJsons) {
    // Normally the ScriptStartedEventJson should be the first in the list so this should be quick
    return (ScriptStartedEventJson) scriptExecutionEventJsons.stream()
      .filter(eventJson->(eventJson instanceof ScriptStartedEventJson))
      .findFirst()
      .get();
  }

  public List<ScriptExecution> recoverCrashedScriptExecutions() {
    List<ScriptExecution> scriptExecutions = new ArrayList<>();
    Map<String,List<ExecutionEventJson>> groupedEvents = findCrashedScriptExecutionEvents();
    for (String scriptExecutionId: groupedEvents.keySet()) {
      List<ExecutionEventJson> scriptExecutionEvents = groupedEvents.get(scriptExecutionId);
      ScriptExecution scriptExecution = recreateScriptExecution(scriptExecutionEvents, scriptExecutionId);
      scriptExecutions.add(scriptExecution);
    }
    return scriptExecutions;
  }

  /** @return a list of events grouped by script execution. */
  public Map<String,List<ExecutionEventJson>> findCrashedScriptExecutionEvents() {
    Map<String,List<ExecutionEventJson>> groupedEvents = new HashMap<>();
    for (EventJson event: getEvents()) {
      if (event instanceof ExecutionEventJson) {
        ExecutionEventJson executableEvent = (ExecutionEventJson) event;
        String scriptExecutionId = executableEvent.getScriptExecutionId();
        if (executableEvent instanceof ScriptEndedEventJson) {
          groupedEvents.remove(scriptExecutionId);
        } else {
          List<ExecutionEventJson> scriptExecutionEvents = groupedEvents.get(scriptExecutionId);
          if (scriptExecutionEvents==null) {
            scriptExecutionEvents = new ArrayList<>();
            groupedEvents.put(scriptExecutionId, scriptExecutionEvents);
          }
          scriptExecutionEvents.add(executableEvent);
        }
      }
    }
    for (String scriptExecutionId: new ArrayList<>(groupedEvents.keySet())) {
      List<ExecutionEventJson> scriptExecutionEvents = groupedEvents.get(scriptExecutionId);
      ExecutionEventJson lastEventJson = scriptExecutionEvents.get(scriptExecutionEvents.size()-1);
      if (isUnlocking(lastEventJson)) {
        groupedEvents.remove(scriptExecutionId);
      }
    }
    return groupedEvents;
  }

  private boolean isExecutable(ExecutionEventJson eventJson) {
    return (eventJson instanceof ExecutableEventJson);
  }

  private boolean isUnlocking(ExecutionEventJson lastEventJson) {
    return lastEventJson!=null
      && ( lastEventJson instanceof ActionWaitingEventJson
           || lastEventJson instanceof ScriptEndedEventJson);
  }

  public String eventToJsonString(ExecutionEvent event) {
    return eventJsonToJsonString(event.toJson());
  }

  public String eventJsonToJsonString(EventJson eventJson) {
    return eventJson!=null ? engineConfiguration.getGson().toJson(eventJson) : "null";
  }

  public Object valueToJson(Object value) {
    if (value==null) {
      return "null";
    }
    if (value instanceof Action) {
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



  private <T extends EventJson> List<T> coldReplayEvents(String stream, Class<T> eventType) {
    List<T> events = new ArrayList<>();

    CompletableFuture<List<T>> future = new CompletableFuture<>();

    evClient.replay(stream, EventReplayMode.REPLAY_ONLY, new Subscriber<io.muoncore.protocol.event.Event>() {
      @Override
      public void onSubscribe(Subscription subscription) {
        //todo, can smooth the replay flow here if wanted. use some higher level subscriber to do that.
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(io.muoncore.protocol.event.Event event) {

        try {
          @SuppressWarnings("unchecked")
          Class<T> type = (Class<T>) Class.forName(event.getEventType());
          T payload = event.getPayload(type);
          events.add(payload);
          log.info("Replayed {}", payload);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onError(Throwable throwable) {
        log.warn("Failed replay of {}", stream);
        future.completeExceptionally(throwable);
      }

      @Override
      public void onComplete() {
        log.info("Completed replay of {}", stream);
        future.complete(events);
      }
    });

    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new IllegalStateException("Failed loading event stream " + stream, e);
    }
  }
}

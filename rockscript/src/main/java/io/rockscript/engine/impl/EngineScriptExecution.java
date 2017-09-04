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

import io.rockscript.engine.EngineException;
import io.rockscript.engine.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/** The runtime state of a engineScript execution. */
public class EngineScriptExecution extends BlockExecution<EngineScript> {

  static Logger log = LoggerFactory.getLogger(EngineScriptExecution.class);

  EventListener eventListener;
  int nextInternalExecutionId = 1;
  ExecutionMode executionMode;
  boolean isEnded;
  Queue<Operation> work = new LinkedList<Operation>();

  LinkedList<ExecutionEvent> unreplayedEvents;

  public EngineScriptExecution(String scriptExecutionId, Configuration configuration, EngineScript engineScript) {
    super(scriptExecutionId, engineScript, null);
    this.eventListener = configuration.getEventListener();
    this.executionMode = ExecutionMode.EXECUTING;
    initializeSystemVariable(configuration);
  }

  public EngineScriptExecution(String scriptExecutionId, Configuration configuration, EngineScript engineScript, List<ExecutionEvent> storedEvents) {
    this(scriptExecutionId, configuration, engineScript);

    this.executionMode = ExecutionMode.REBUILDING;
    this.unreplayedEvents = new LinkedList<>(storedEvents);

    while (!this.unreplayedEvents.isEmpty()) {
      ExecutableEvent executableEvent = (ExecutableEvent) removeNextUnreplayedEvent();
      // Script execution events do not have an executionId in the event, only the scriptExecutionId.
      Execution execution = executableEvent.executionId!=null ? findExecutionRecursive(executableEvent.executionId) : this;
      executableEvent.execute(execution);
    }

    this.executionMode = ExecutionMode.EXECUTING;
    this.unreplayedEvents = null;
  }

  private void initializeSystemVariable(Configuration configuration) {
    JsonObject systemJsonObject = new JsonObject();
    systemJsonObject.put("import", new SystemImportActivity(configuration));
    createVariable("system")
      .setValue(systemJsonObject);
  }

  void setInput(Object input) {
    JsonObject systemJsonObject = (JsonObject) getVariable("system").getValue();
    systemJsonObject.put("input", input);
  }

  public void doWork() {
    while (!work.isEmpty()) {
      Operation operation = work.poll();
      operation.execute(this);
    }
  }

  public void addWork(Operation operation) {
    work.add(operation);
  }

  @Override
  protected void dispatch(ExecutionEvent event) {
    if (!rebuilding()) {
      eventListener.handle(event);
    } else {
      if (!(event instanceof ExecutableEvent)) {
        ExecutionEvent unreplayedEvent = removeNextUnreplayedEvent();
        if (!event.toString().equals(unreplayedEvent.toString())) {
          log.debug("Expected, unreplayed event: "+unreplayedEvent.toString());
          throw new RuntimeException();
        }
      }
    }
  }

  private ExecutionEvent removeNextUnreplayedEvent() {
    ExecutionEvent nextUnreplayedEvent = unreplayedEvents.removeFirst();
    if (unreplayedEvents.isEmpty()) {
      executionMode = ExecutionMode.EXECUTING;
    } else if (unreplayedEvents.size() == 1) {
      executionMode = ExecutionMode.RECOVERING;
    }
    return nextUnreplayedEvent;
  }

  protected void dispatchAndExecute(ExecutableEvent event, Execution execution) {
    dispatch(event);

    if (!rebuilding()) {
      addWork(new ExecuteEventOperation(event, execution));
      // event.execute(execution);
    } // when rebuilding, the loop in the constructor will re-execute the ExecutableEvent's
  }

  private boolean rebuilding() {
    return executionMode==ExecutionMode.REBUILDING
           || executionMode==ExecutionMode.RECOVERING;
  }

  public String createInternalExecutionId() {
    return "e"+Integer.toString(nextInternalExecutionId++);
  }

  public void start(Object input) {
    dispatchAndExecute(new ScriptStartedEvent(this, input));
    // Continues at startExecute()
  }

  // Continuation from start(Object)
  public void startExecute() {
    start();
  }

  @Override
  protected void end() {
    this.isEnded = true;
    dispatch(new ScriptEndedEvent(this));
  }

  @Override
  public EngineScriptExecution getScriptExecution() {
    return this;
  }

  @Override
  public EngineScript getEngineScript() {
    return element;
  }

  public void endFunctionInvocationExecution(String executionId) {
    endFunctionInvocationExecution(executionId, null);
  }

  public void endFunctionInvocationExecution(String executionId, Object result) {
    ArgumentsExpressionExecution argumentsExpressionExecution = (ArgumentsExpressionExecution) findExecutionRecursive(executionId);
    EngineException.throwIfNull(argumentsExpressionExecution, "Couldn't find function invocation execution %s in engineScript execution %s", executionId, id);
    argumentsExpressionExecution.endActivityExecute(result);
  }

  public EventListener getEventListener() {
    return eventListener;
  }

  public void setEventListener(EventListener eventListener) {
    this.eventListener = eventListener;
  }

  public EngineScript getElement() {
    return element;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ExecutionMode getExecutionMode() {
    return executionMode;
  }

  public void setExecutionMode(ExecutionMode executionMode) {
    this.executionMode = executionMode;
  }

  public boolean isEnded() {
    return isEnded;
  }
}

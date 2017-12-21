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
import io.rockscript.api.model.ScriptExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

/** The runtime state of a engineScript execution. */
public class EngineScriptExecution extends BlockExecution<EngineScript> {

  static Logger log = LoggerFactory.getLogger(EngineScriptExecution.class);

  EventDispatcher eventDispatcher;
  int nextInternalExecutionId = 1;
  ExecutionMode executionMode;
  Instant start;
  Instant end;
  Queue<Operation> work = new LinkedList<Operation>();

  public EngineScriptExecution(String scriptExecutionId, Engine engine, EngineScript engineScript) {
    super(scriptExecutionId, engineScript, null);
    this.eventDispatcher = engine.getEventDispatcher();
    this.executionMode = ExecutionMode.EXECUTING;
    initializeSystemVariable(engine);
  }

  private void initializeSystemVariable(Engine engine) {
    JsonObject systemJsonObject = new JsonObject();
    systemJsonObject.put("import", new SystemImportServiceFunction(engine));
    createVariable("system")
      .setValue(systemJsonObject);
  }

  public void setInput(Object input) {
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
    if (!isReplaying()) {
      eventDispatcher.dispatch(event);
    }
  }

  protected void dispatchAndExecute(ExecutableEvent event, Execution execution) {
    if (isRecovering() && event.getClass()==ServiceFunctionStartedEvent.class) {
      executionMode = ExecutionMode.EXECUTING;
    }
    // while replaying events don't need to be dispatched or executed again
    if (!isReplaying()) {
      dispatch(event);
      addWork(new ExecuteEventOperation(event, execution));
    }
  }

  private boolean isReplaying() {
    return executionMode==ExecutionMode.REPLAYING;
  }

  private boolean isRecovering() {
    return executionMode==ExecutionMode.RECOVERING;
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
    dispatchAndExecute(new ScriptEndedEvent(this));
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
    argumentsExpressionExecution.endFunctionExecute(result);
  }

  public EventDispatcher getEventDispatcher() {
    return eventDispatcher;
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
    return end!=null;
  }

  public Instant getEnded() {
    return end;
  }

  public void setEnd(Instant end) {
    this.end = end;
  }

  public void setStart(Instant start) {
    this.start = start;
  }

  public Instant getStart() {
    return start;
  }

  public ScriptExecution toScriptExecution() {
    return new ScriptExecution(this);
  }
}

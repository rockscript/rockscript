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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The runtime state of a script execution. */
public class ScriptExecution extends BlockExecution<Script> {

  static Logger log = LoggerFactory.getLogger(ScriptExecution.class);

  EventListener eventListener;
  int nextInternalExecutionId = 1;
  ExecutionMode executionMode;

  public ScriptExecution(String scriptExecutionId, EngineConfiguration engineConfiguration, Script script) {
    super(scriptExecutionId, script, null);
    this.eventListener = engineConfiguration.getEventListener();
    this.executionMode = ExecutionMode.EXECUTING;
    initializeSystemVariable(engineConfiguration);
  }

  private void initializeSystemVariable(EngineConfiguration engineConfiguration) {
    JsonObject systemJsonObject = new JsonObject();
    systemJsonObject.put("import", new SystemImportActivity(engineConfiguration));
    createVariable("system")
      .setValue(systemJsonObject);
  }

  void setInput(Object input) {
    JsonObject systemJsonObject = (JsonObject) getVariable("system").getValue();
    systemJsonObject.put("input", input);
  }

  public String createInternalExecutionId() {
    return "e"+Integer.toString(nextInternalExecutionId++);
  }

  @Override
  public void start() {
    executeNextStatement();
  }

  @Override
  protected void end() {
    dispatch(new ScriptEndedEvent(this));
  }

  @Override
  protected void dispatch(ExecutionEvent event) {
    eventListener.handle(event);
  }

  @Override
  public ScriptExecution getScriptExecution() {
    return this;
  }

  @Override
  public Script getScript() {
    return element;
  }

  public void endFunctionInvocationExecution(String executionId) {
    endFunctionInvocationExecution(executionId, null);
  }

  public void endFunctionInvocationExecution(String executionId, Object result) {
    ArgumentsExpressionExecution argumentsExpressionExecution = (ArgumentsExpressionExecution) findExecutionRecursive(executionId);
    ScriptException.throwIfNull(argumentsExpressionExecution, "Couldn't find function invocation execution %s in script execution %s", executionId, id);
    argumentsExpressionExecution.endActivityExecute(result);
  }

  public EventListener getEventListener() {
    return eventListener;
  }

  public void setEventListener(EventListener eventListener) {
    this.eventListener = eventListener;
  }

  public Script getElement() {
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
}

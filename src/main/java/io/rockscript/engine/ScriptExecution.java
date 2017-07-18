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

import io.rockscript.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The runtime state of a script execution, aka 'an execution'. */
public class ScriptExecution extends BlockExecution<Script> {

  static Logger log = LoggerFactory.getLogger(ScriptExecution.class);

  ServiceLocator serviceLocator;
  EventListener eventListener;
  int nextInternalExecutionId = 1;
  ExecutionMode executionMode;

  public ScriptExecution(String scriptExecutionId, ServiceLocator serviceLocator, Script script) {
    super(scriptExecutionId, script, null);
    this.serviceLocator = serviceLocator;
    this.eventListener = serviceLocator.getEventListener();
    this.executionMode = ExecutionMode.EXECUTING;
    initializeSystemVariable(serviceLocator);
  }

  private void initializeSystemVariable(ServiceLocator serviceLocator) {
    Variable systemVariable = createVariable("system");
    JsonObject systemJsonObject = new JsonObject();
    systemJsonObject.put("import", new SystemImportAction(serviceLocator));
    systemVariable.setValue(systemJsonObject);
  }

  public String createInternalExecutionId() {
    return "e"+Integer.toString(nextInternalExecutionId++);
  }

  @Override
  public void start() {
    dispatchAndExecute(new StartScriptEvent(this));
    // Executing the event will continue with this.startExecute()
  }

  // Continuation of start
  void startExecute() {
    executeNextStatement();
  }

  @Override
  protected void end() {
    dispatch(new EndScriptEvent(this));
  }

  @Override
  protected void dispatch(Event event) {
    if (eventListener!=null && this.executionMode==ExecutionMode.EXECUTING) {
      eventListener.handle(event);
    } else {
      log.debug("swallowing: "+serviceLocator.getEventStore().toJson(event));
    }
  }

  @Override
  public ScriptExecution getScriptExecution() {
    return this;
  }

  @Override
  public Script getScript() {
    return operation;
  }

  public EventListener getEventListener() {
    return eventListener;
  }

  public void setEventListener(EventListener eventListener) {
    this.eventListener = eventListener;
  }

  public Script getOperation() {
    return operation;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void endFunctionInvocationExecution(String executionId) {
    endFunctionInvocationExecution(executionId, null);
  }

  public void endFunctionInvocationExecution(String executionId, Object result) {
    ArgumentsExpressionExecution argumentsExpressionExecution = (ArgumentsExpressionExecution) findExecutionRecursive(executionId);
    ScriptException.throwIfNull(argumentsExpressionExecution, "Couldn't find function invocation execution %s in script execution %s", executionId, id);
    argumentsExpressionExecution.endActionExecute(result);
  }
}

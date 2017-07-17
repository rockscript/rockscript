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

import java.util.List;
import java.io.File;

import io.rockscript.*;

public class EngineImpl implements Engine {

  String lockClientId;
  protected ServiceLocator serviceLocator;

  public EngineImpl(ServiceLocator serviceLocator, String lockClientId) {
    serviceLocator.throwIfNotProperlyConfigured();
    this.serviceLocator = serviceLocator;
    this.lockClientId = lockClientId;
  }

  public String deployScript(String scriptText) {
    Script script = deployScriptImpl(scriptText);
    return script.getId();
  }

  public String deployScript(File script) {
    throw new RuntimeException("deployScript(File) not implemented");
  }

  public Script deployScriptImpl(String scriptText) {
    Script script = parseScript(scriptText);
    storeScript(script, scriptText);
    return script;
  }

  protected Script parseScript(String scriptText) {
    Script script = Parse.parse(scriptText);
    script.setServiceLocator(serviceLocator);
    return script;
  }

  private void storeScript(Script script, String scriptText) {
    serviceLocator
      .getScriptStore()
      .saveScript(script, scriptText);
  }

  public String startScriptExecution(String scriptId) {
    ScriptExecution scriptExecution = startScriptExecutionImpl(scriptId);
    return scriptExecution.getId();
  }

  public ScriptExecution startScriptExecutionImpl(String scriptId) {
    Script script = serviceLocator
      .getScriptStore()
      .loadScript(scriptId);

    String scriptExecutionId = serviceLocator
        .getScriptExecutionIdGenerator()
        .generateId();

    ScriptExecution scriptState = new ScriptExecution(scriptExecutionId, serviceLocator, script);

    serviceLocator
      .getLockService()
      .newScriptExecution(scriptState, lockClientId);

    scriptState.start();

    return scriptState;
  }

  public void endWaitingExecutionId(String scriptExecutionId, String waitingExecutionId) {
    endWaitingExecutionId(scriptExecutionId, waitingExecutionId, null);
  }

  public void endWaitingExecutionId(String scriptExecutionId, String waitingExecutionId, Object result) {
    endWaitingExecutionImpl(scriptExecutionId, waitingExecutionId, result);
  }

  public ScriptExecution endWaitingExecutionImpl(String scriptExecutionId, String waitingExecutionId, Object result) {
    ScriptExecution scriptExecution = serviceLocator
      .getEventStore()
      .loadScriptExecution(scriptExecutionId);

    ArgumentsExpressionExecution execution = (ArgumentsExpressionExecution) scriptExecution
      .findExecutionRecursive(waitingExecutionId);

    execution.functionEnded(result);

    return scriptExecution;
  }

  public ServiceLocator getServiceLocator() {
    return serviceLocator;
  }

  @Override
  public List<ScriptExecution> recoverCrashedScriptExecutions() {
    return serviceLocator
      .getEventStore()
      .recoverCrashedScriptExecutions();
  }
}

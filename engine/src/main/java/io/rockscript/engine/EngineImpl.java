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

import java.io.File;
import java.util.List;

import com.google.inject.*;
import io.rockscript.Engine;
import io.rockscript.engine.test.TestLockService;

public abstract class EngineImpl implements Engine {

  protected EngineConfiguration engineConfiguration;

  public EngineImpl(EngineConfiguration engineConfiguration) {
    this.engineConfiguration = engineConfiguration;
    this.engineConfiguration.throwIfNotProperlyConfigured();
  }

  public String deployScript(String scriptText) {
    Script script = deployScriptImpl(scriptText);
    return script.getId();
  }

  public String deployScript(File script) {
    throw new RuntimeException("deployScript(File) not implemented");
  }

  public Script deployScriptImpl(String scriptText) {
    String scriptId = engineConfiguration.getScriptIdGenerator().createId();
    Script script = parseScript(scriptText);
    script.setId(scriptId);
    storeScript(script, scriptText);
    engineConfiguration
      .getEventStore()
      .handle(new ScriptDeployedEvent(script, scriptText));
    return script;
  }

  protected Script parseScript(String scriptText) {
    Script script = Parse.parse(scriptText);
    script.setEngineConfiguration(engineConfiguration);
    return script;
  }

  private void storeScript(Script script, String scriptText) {
    engineConfiguration
      .getScriptStore()
      .saveScript(script, scriptText);
  }

  public String startScriptExecution(String scriptId) {
    ScriptExecution scriptExecution = startScriptExecutionImpl(scriptId);
    return scriptExecution.getId();
  }

  public ScriptExecution startScriptExecutionImpl(String scriptId) {
    Script script = engineConfiguration
      .getScriptStore()
      .loadScript(scriptId);

    String scriptExecutionId = engineConfiguration
        .getScriptExecutionIdGenerator()
        .createId();

    ScriptExecution scriptState = new ScriptExecution(scriptExecutionId, engineConfiguration, script);

    engineConfiguration
      .getLockService()
      .newScriptExecution(scriptState, "localhost");

    scriptState.start();

    return scriptState;
  }

  @Override
  public void endWaitingAction(ScriptExecutionContext context) {
    endWaitingAction(context, null);
  }

  @Override
  public void endWaitingAction(ScriptExecutionContext context, Object result) {
    endWaitingActionImpl(context, result);
  }

  public ScriptExecution endWaitingActionImpl(ScriptExecutionContext context, Object result) {
    ScriptExecution scriptExecution = engineConfiguration
      .getEventStore()
      .loadScriptExecution(context.scriptExecutionId);

    String waitingExecutionId = context.executionId;
    ArgumentsExpressionExecution execution = (ArgumentsExpressionExecution) scriptExecution
      .findExecutionRecursive(waitingExecutionId);

    execution.endAction(result);

    return scriptExecution;
  }

  public EngineConfiguration getEngineConfiguration() {
    return engineConfiguration;
  }

  @Override
  public List<ScriptExecution> recoverCrashedScriptExecutions() {
    return engineConfiguration
      .getEventStore()
      .recoverCrashedScriptExecutions();
  }
}

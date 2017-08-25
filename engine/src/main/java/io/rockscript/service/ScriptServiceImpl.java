/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
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

package io.rockscript.service;

import io.rockscript.DeployScriptCommand;
import io.rockscript.ScriptService;
import io.rockscript.engine.EngineConfiguration;
import io.rockscript.engine.ScriptExecution;

import java.util.List;

public abstract class ScriptServiceImpl implements ScriptService {

  protected EngineConfiguration engineConfiguration;

  public ScriptServiceImpl(EngineConfiguration engineConfiguration) {
    engineConfiguration.seal(this);
    this.engineConfiguration = engineConfiguration;
  }

  public DeployScriptCommand newDeployScriptCommand() {
    return new DeployScriptCommandImpl(engineConfiguration);
  }

//  public ScriptAst deployScript(String scriptText) {
//    String scriptId = engineConfiguration.getScriptIdGenerator().createId();
//    ScriptAst scriptAst = parseScript(scriptText);
//    scriptAst.setId(scriptId);
//    storeScript(scriptAst, scriptText);
//    return scriptAst;
//  }
//
//  protected ScriptAst parseScript(String scriptText) {
//    ScriptAst scriptAst = Parse.parse(scriptText);
//    scriptAst.setEngineConfiguration(engineConfiguration);
//    return scriptAst;
//  }
//
//  private void storeScript(ScriptAst scriptAst, String scriptText) {
//    engineConfiguration
//      .getScriptStore()
//      .saveScript(scriptAst, scriptText);
//  }

  public ScriptExecution startScriptExecution(String scriptId) {
    return startScriptExecution(scriptId, null);
  }

  public ScriptExecution startScriptExecution(String scriptId, Object input) {
    return engineConfiguration
      .getEngine()
      .startScriptExecution(scriptId, input);
  }

  @Override
  public ScriptExecution endActivity(String scriptExecutionId, String executionId) {
    return endActivity(scriptExecutionId, executionId, null);
  }

  @Override
  public ScriptExecution endActivity(String scriptExecutionId, String executionId, Object result) {
    return engineConfiguration
      .getEngine()
      .endActivity(scriptExecutionId, executionId, result);
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

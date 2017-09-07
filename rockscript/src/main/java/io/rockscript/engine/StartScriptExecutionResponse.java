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
package io.rockscript.engine;


import io.rockscript.engine.impl.EngineScriptExecution;

public class StartScriptExecutionResponse implements CommandResponse {

  String scriptExecutionId;

  /** transient because it must not be serialized with Gson */
  transient EngineScriptExecution engineScriptExecution;

  /** constructor for Gson serialization */
  StartScriptExecutionResponse() {
  }

  public StartScriptExecutionResponse(EngineScriptExecution engineScriptExecution) {
    this.engineScriptExecution = engineScriptExecution;
    this.scriptExecutionId = engineScriptExecution.getId();
  }

  public String getScriptExecutionId() {
    return scriptExecutionId;
  }

  public ScriptExecution getScriptExecution() {
    return new ScriptExecution(engineScriptExecution);
  }

  public EngineScriptExecution getEngineScriptExecution() {
    return engineScriptExecution;
  }

  @Override
  public int getStatus() {
    return 200;
  }
}

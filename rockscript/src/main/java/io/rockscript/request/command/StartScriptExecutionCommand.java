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
package io.rockscript.request.command;

import io.rockscript.engine.Configuration;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.request.CommandImpl;

import java.util.LinkedHashMap;
import java.util.Map;

/** Command to start a new script execution.
 * StartScriptExecutionCommand's are serializable with Gson */
public class StartScriptExecutionCommand extends CommandImpl<EngineStartScriptExecutionResponse> {

  protected String scriptName;
  protected String scriptId;
  protected Object input;

  public StartScriptExecutionCommand() {
  }

  public StartScriptExecutionCommand(Configuration configuration) {
    super(configuration);
  }

  @Override
  protected EngineStartScriptExecutionResponse execute(Configuration configuration) {
    EngineScriptExecution engineScriptExecution = configuration
      .getEngine()
      .startScriptExecution(scriptName, scriptId, input);
    return new EngineStartScriptExecutionResponse(engineScriptExecution);
  }

  public String getScriptName() {
    return this.scriptName;
  }
  public void setScriptName(String scriptName) {
    this.scriptName = scriptName;
  }
  /** (Optional, but this or {@link #scriptId(String)} is Required) the script
   * for which the latest version will be started. */
  public StartScriptExecutionCommand scriptName(String scriptName) {
    this.scriptName = scriptName;
    return this;
  }


  public String getScriptId() {
    return this.scriptId;
  }
  public void setScriptId(String scriptId) {
    this.scriptId = scriptId;
  }
  /** (Optional, but this or {@link #scriptName(String)} is Required) the specific
   * script version that has to be started. */
  public StartScriptExecutionCommand scriptId(String scriptId) {
    this.scriptId = scriptId;
    return this;
  }

  public Object getInput() {
    return this.input;
  }
  public void setInput(Object input) {
    this.input = input;
  }
  /** (Optional) the input data for the script that will be made
   * available in the script under system.input */
  public StartScriptExecutionCommand input(Object input) {
    this.input = input;
    return this;
  }

  public StartScriptExecutionCommand inputProperty(String propertyName, Object propertyValue) {
    if (input==null) {
      input = new LinkedHashMap<String,Object>();
    }
    if (!(input instanceof Map)) {
      throw new RuntimeException("inputProperty can only be used with maps / script objects");
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> objectMap = (Map<String, Object>) this.input;
    objectMap.put(propertyName, propertyValue);
    return this;
  }

}

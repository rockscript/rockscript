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
package io.rockscript.api.commands;

import io.rockscript.api.model.Script;
import io.rockscript.engine.Configuration;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.api.Command;
import io.rockscript.engine.impl.ScriptStore;
import io.rockscript.netty.router.BadRequestException;

import java.util.LinkedHashMap;
import java.util.Map;

/** AsyncHttpRequest to start a new script execution.
 * StartScriptExecutionCommand's are serializable with Gson */
public class StartScriptExecutionCommand extends Command<EngineStartScriptExecutionResponse> {

  protected String scriptId;
  protected String scriptName;
  protected String scriptVersionId;
  protected Object input;

  @Override
  public EngineStartScriptExecutionResponse execute(Configuration configuration) {
    if (scriptVersionId==null) {
      ScriptStore scriptStore = configuration.getScriptStore();
      Script script = null;
      if (scriptId!=null) {
        script = scriptStore.findScriptById(scriptId);
        BadRequestException.throwIfNull(script, "No script found with id %s", scriptId);
      } else if (scriptName!=null) {
        script = scriptStore.findScriptByNameEnd(scriptName);
        BadRequestException.throwIfNull(script, "No script found with name %s", scriptName);
      } else {
        throw new BadRequestException("No script version specified. Please provide one of scriptId, scriptName or scriptVersionId in the command");
      }
      scriptVersionId = script.getActiveScriptVersionId();
      BadRequestException.throwIfNull(scriptVersionId, "Script %s does not have an active version yet", scriptId);
    }

    EngineScriptExecution engineScriptExecution = configuration
      .getEngine()
      .startScriptExecution(scriptVersionId, input);
    return new EngineStartScriptExecutionResponse(engineScriptExecution);
  }

  public String getScriptId() {
    return this.scriptId;
  }
  public void setScriptId(String scriptId) {
    this.scriptId = scriptId;
  }
  public StartScriptExecutionCommand scriptId(String scriptId) {
    this.scriptId = scriptId;
    return this;
  }

  public String getScriptName() {
    return this.scriptName;
  }
  public void setScriptName(String scriptName) {
    this.scriptName = scriptName;
  }
  /** (Optional, but this or {@link #scriptVersionId(String)} is Required) the script
   * for which the latest version will be started. */
  public StartScriptExecutionCommand scriptName(String scriptName) {
    this.scriptName = scriptName;
    return this;
  }


  public String getScriptVersionId() {
    return this.scriptVersionId;
  }
  public void setScriptVersionId(String scriptVersionId) {
    this.scriptVersionId = scriptVersionId;
  }
  /** (Optional, but this or {@link #scriptName(String)} is Required) the specific
   * script version that has to be started. */
  public StartScriptExecutionCommand scriptVersionId(String scriptVersionId) {
    this.scriptVersionId = scriptVersionId;
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

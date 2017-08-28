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
package io.rockscript;

import io.rockscript.engine.EngineScript;
import io.rockscript.engine.Parse;
import io.rockscript.engine.ScriptStore;
import io.rockscript.service.CommandImpl;
import io.rockscript.service.Configuration;

/** Deploys a new script version to the script service.
 *
 * Required fields: {@link #scriptText(String)}
 *
 * Example usage:
 * Use it like this:
 * <code>
 *   DeployScriptResponse response = scriptService.newDeployScriptCommand()
 *     .name("Approval")
 *     .text("...the script text...")
 *     .execute();
 * </code>
 *
 * DeployScriptCommand's are serializable with Gson.
 */
public class DeployScriptCommand extends CommandImpl<DeployScriptResponse> {

  protected String scriptName;
  protected String scriptText;

  /** used by gson deserialization */
  public DeployScriptCommand() {
  }

  public DeployScriptCommand(Configuration configuration) {
    super(configuration);
  }

  @Override
  protected DeployScriptResponse execute(Configuration configuration) {
    ScriptStore scriptStore = configuration.getScriptStore();
    DeployScriptResponse response = new DeployScriptResponse();
    response.setText(scriptText);

    Parse parse = Parse.create(scriptText);
    if (!parse.hasErrors()) {
      String id = configuration.getScriptIdGenerator().createId();
      response.setId(id);

      if (scriptName ==null) {
        scriptName = "Unnamed response";
      }
      response.setName(scriptName);

      EngineScript engineScript = parse.getEngineScript();
      scriptStore.deploy(response, engineScript);

    } else {
      response.setErrors(parse.getErrors());
    }
    return response;
  }

  public String getScriptName() {
    return this.scriptName;
  }
  public void setScriptName(String scriptName) {
    this.scriptName = scriptName;
  }
  /** (Optional) the script name */
  public DeployScriptCommand scriptName(String scriptName) {
    this.scriptName = scriptName;
    return this;
  }

  public String getScriptText() {
    return this.scriptText;
  }
  public void setScriptText(String scriptText) {
    this.scriptText = scriptText;
  }
  /** (Required) the script text */
  public DeployScriptCommand scriptText(String scriptText) {
    this.scriptText = scriptText;
    return this;
  }
}

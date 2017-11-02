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

import io.rockscript.engine.Configuration;
import io.rockscript.engine.Script;
import io.rockscript.engine.impl.EngineScript;
import io.rockscript.engine.impl.Parse;
import io.rockscript.engine.impl.ScriptStore;
import io.rockscript.api.Command;

/** Deploys a new script version to the script service.
 *
 * Required fields: {@link #scriptText(String)}
 *
 * Example usage:
 * Use it like this:
 * <code>
 *   EngineDeployScriptResponse response = scriptService.newDeployScriptCommand()
 *     .name("Approval")
 *     .text("...the script text...")
 *     .execute();
 * </code>
 *
 * DeployScriptCommand's are serializable with Gson.
 */
public class DeployScriptCommand extends Command<EngineDeployScriptResponse> {

  protected String scriptName;
  protected String scriptText;

  @Override
  public EngineDeployScriptResponse execute(Configuration configuration) {
    ScriptStore scriptStore = configuration.getScriptStore();
    String scriptName1 = scriptName;
    Script script = new Script();
    script.setText(scriptText);

    if (scriptName1==null) {
      scriptName1 = "Unnamed script";
    }
    script.setName(scriptName1);

    Parse parse = scriptStore.parseScript(script);
    if (!parse.hasErrors()) {
      String id = script.getId();
      if (id==null) {
        id = configuration.getScriptIdGenerator().createId();
        script.setId(id);
      }

      EngineScript engineScript = parse.getEngineScript();
      scriptStore.scriptAstsById.put(id, engineScript);

      // storeScript also assigns the version
      scriptStore.storeScript(script);
    }

    return new EngineDeployScriptResponse(script, parse.getErrors());
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

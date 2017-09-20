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
package io.rockscript.engine.impl;

import io.rockscript.engine.Script;

public class ScriptStartedEvent extends ExecutableEvent<EngineScriptExecution> {

  String scriptId;
  String scriptName;
  Object input;

  /** constructor for gson serialization */
  ScriptStartedEvent() {
  }

  @Override
  public void execute(EngineScriptExecution execution) {
    execution.setInput(input);
    execution.setStart(time);
    execution.startExecute();
  }

  public ScriptStartedEvent(EngineScriptExecution scriptExecution, Object input) {
    super(scriptExecution);

    Script script = scriptExecution
        .getEngineScript()
        .getScript();

    this.scriptId = script.getId();
    this.scriptName = script.getName();
    this.input = input;
  }

  public String getScriptId() {
    return scriptId;
  }

  public String getScriptExecutionId() {
    return scriptExecutionId;
  }

  public Object getInput() {
    return input;
  }

  @Override
  public String toString() {
    return "[" + scriptExecutionId + "] " +
        "Started script [" +
        scriptId + "|" +
        scriptName + "]" +
        (input!=null ? " with input "+input.toString() : " without input");
  }
}

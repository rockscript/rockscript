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

import java.time.Instant;

import static io.rockscript.engine.ScriptException.throwIfNull;

public class Lock {

  Instant createTime;
  String scriptExecutionId;
  String clientId;
  ScriptExecution scriptExecution;

  public Lock(ScriptExecution scriptExecution, String clientId) {
    this(scriptExecution.getId(), clientId);
    this.scriptExecution = scriptExecution;
    this.createTime = Instant.now();
  }

  public Lock(String scriptExecutionId, String clientId) {
    this.scriptExecutionId = ScriptException.throwIfNull(scriptExecutionId);
    this.clientId = ScriptException.throwIfNull(clientId);
  }

  public String getScriptExecutionId() {
    return scriptExecutionId;
  }

  public String getClientId() {
    return clientId;
  }

  public ScriptExecution getScriptExecution() {
    return scriptExecution;
  }

  public void setScriptExecution(ScriptExecution scriptExecution) {
    if (!scriptExecutionId.equals(scriptExecution.getId())) {
      throw new ScriptException("The lock's script execution id ("+scriptExecutionId+") differs from the given scriptExecution.getId() ("+scriptExecution.getId()+")");
    }
    this.scriptExecution = scriptExecution;
  }
}


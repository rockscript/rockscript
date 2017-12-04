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
package io.rockscript.engine.impl;

/** A pointer to a position in the script execution
 * where it is waiting for a service function to complete.  */
public class ContinuationReference {

  String scriptExecutionId;
  String executionId;

  public ContinuationReference() {
  }

  public ContinuationReference(String scriptExecutionId, String executionId) {
    this.scriptExecutionId = scriptExecutionId;
    this.executionId = executionId;
  }

  public ContinuationReference(Execution execution) {
    this.scriptExecutionId = execution.getScriptExecution().getId();
    this.executionId = execution.getId();
  }

  public String getScriptExecutionId() {
    return scriptExecutionId;
  }

  public void setScriptExecutionId(String scriptExecutionId) {
    this.scriptExecutionId = scriptExecutionId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
}

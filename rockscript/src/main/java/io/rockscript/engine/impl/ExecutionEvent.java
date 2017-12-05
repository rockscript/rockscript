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

import java.time.Instant;

public abstract class ExecutionEvent<T extends Execution> implements Event {

  protected Instant time;
  protected String scriptExecutionId;
  protected String executionId;
  protected Integer line;

  /** for Gson serialzation */
  ExecutionEvent() {
  }

  public ExecutionEvent(T execution) {
    this.time = Time.now();
    this.scriptExecutionId = execution.getScriptExecution().getId();
    if (! (execution instanceof EngineScriptExecution)) {
      this.executionId = execution.getId();
      this.line = execution.getElement().getLocation().getLine();
    }
  }

  public Instant getTime() {
    return time;
  }

  public String getScriptExecutionId() {
    return scriptExecutionId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public Integer getLine() {
    return line;
  }
}

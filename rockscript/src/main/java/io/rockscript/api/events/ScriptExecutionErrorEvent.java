/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.rockscript.api.events;

import io.rockscript.engine.impl.Execution;

public class ScriptExecutionErrorEvent extends ExecutionEvent<Execution> {

  String error;
  String scriptVersionId;

  /**
   * constructor for gson deserialization
   */
  ScriptExecutionErrorEvent() {
  }

  public ScriptExecutionErrorEvent(Execution execution, String error) {
    super(execution);
    this.scriptVersionId = execution.getEngineScript().getScriptVersion().getId();
    this.error = error;
  }

  @Override
  public boolean isUnlocking() {
    return true;
  }

  public String getError() {
    return error;
  }

  @Override
  public String toString() {
    return "[" + scriptExecutionId + "] " +
           "Error [script:" + scriptVersionId + ",line:" + line + "] " + error;
  }

  public String getScriptVersionId() {
    return scriptVersionId;
  }
}

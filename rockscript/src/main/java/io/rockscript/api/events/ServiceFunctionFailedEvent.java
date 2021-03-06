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

import io.rockscript.engine.impl.ArgumentsExpressionExecution;

import java.time.Instant;

public class ServiceFunctionFailedEvent extends ExecutableEvent<ArgumentsExpressionExecution> {

  String scriptVersionId;
  String error;
  Instant retryTime;

  /** constructor for gson deserialization
   * */
  ServiceFunctionFailedEvent() {
  }

  public ServiceFunctionFailedEvent(ArgumentsExpressionExecution execution, String error, Instant retryTime) {
    super(execution);
    this.error = error;
    this.retryTime = retryTime;
    this.scriptVersionId = execution.getEngineScript().getScriptVersion().getId();
  }

  @Override
  public boolean isUnlocking() {
    return true;
  }

  @Override
  public boolean isReplay() {
    return true;
  }

  public Instant getRetryTime() {
    return retryTime;
  }

  public String getError() {
    return error;
  }

  public String getScriptVersionId() {
    return scriptVersionId;
  }

  @Override
  public void execute(ArgumentsExpressionExecution execution) {
    execution.incrementFailedAttemptsCount();
  }

  @Override
  public String toString() {
    return "[" + scriptExecutionId + "|" + executionId + "] " +
           "ServiceFunction failed [script:" +
           scriptVersionId + ",line:" + line + "] " + error + (retryTime!=null ? ", retry scheduled for " + retryTime.toString() : "");
  }
}

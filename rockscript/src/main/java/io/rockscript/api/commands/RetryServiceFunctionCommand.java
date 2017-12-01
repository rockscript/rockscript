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
package io.rockscript.api.commands;

import io.rockscript.Engine;
import io.rockscript.api.Command;
import io.rockscript.engine.impl.ContinuationReference;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.http.servlet.BadRequestException;
import io.rockscript.http.servlet.InternalServerException;

import java.util.LinkedHashMap;
import java.util.Map;

public class RetryServiceFunctionCommand implements Command<ScriptExecutionResponse> {

  protected String scriptExecutionId;
  protected String executionId;

  @Override
  public String getType() {
    return "retryFunction";
  }

  @Override
  public ScriptExecutionResponse execute(Engine engine) {
    BadRequestException.throwIfNull(scriptExecutionId, "scriptExecutionId is a mandatory field");
    BadRequestException.throwIfNull(scriptExecutionId, "executionId is a mandatory field");
    try {
      ContinuationReference continuationReference = new ContinuationReference(scriptExecutionId, executionId);
      EngineScriptExecution engineScriptExecution = engine
          .getScriptRunner()
          .retryFunction(continuationReference);
      return new ScriptExecutionResponse(engineScriptExecution);

    } catch (Exception e) {
      throw new InternalServerException();
    }
  }

  public RetryServiceFunctionCommand continuationReference(ContinuationReference continuationReference) {
    this.scriptExecutionId = continuationReference.getScriptExecutionId();
    this.executionId = continuationReference.getExecutionId();
    return this;
  }

  public String getScriptExecutionId() {
    return this.scriptExecutionId;
  }
  public void setScriptExecutionId(String scriptExecutionId) {
    this.scriptExecutionId = scriptExecutionId;
  }
  public RetryServiceFunctionCommand scriptExecutionId(String scriptExecutionId) {
    this.scriptExecutionId = scriptExecutionId;
    return this;
  }

  public String getExecutionId() {
    return this.executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public RetryServiceFunctionCommand executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }
}

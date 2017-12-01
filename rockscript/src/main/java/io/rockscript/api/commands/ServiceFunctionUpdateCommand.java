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
import io.rockscript.engine.impl.*;
import io.rockscript.engine.job.RetryServiceFunctionJobHandler;
import io.rockscript.http.servlet.BadRequestException;
import io.rockscript.http.servlet.InternalServerException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceFunctionUpdateCommand implements Command<Void> {

  public static final String LEVEL_INFO = "INFO";
  public static final String LEVEL_WARNING = "WARNING";
  public static final String LEVEL_ERROR = "ERROR";

  protected String scriptExecutionId;
  protected String executionId;
  protected String level;
  protected String message;
  protected Instant retry;

  @Override
  public String getType() {
    return "updateFunction";
  }

  @Override
  public Void execute(Engine engine) {
    BadRequestException.throwIfNull(scriptExecutionId, "scriptExecutionId is a mandatory field");
    BadRequestException.throwIfNull(scriptExecutionId, "executionId is a mandatory field");
    try {
      ContinuationReference continuationReference = new ContinuationReference(scriptExecutionId, executionId);

      EventStore eventStore = engine.getEventStore();



//      EngineScriptExecution scriptExecution = eventStore.findScriptExecutionById(scriptExecutionId);
//      ArgumentsExpressionExecution execution = (ArgumentsExpressionExecution) scriptExecution
//        .findExecutionRecursive(executionId);
//
//      execution.update(level, message);
//
//      if (retry!=null) {
//        engine
//          .getJobService()
//          .schedule(new RetryServiceFunctionJobHandler(continuationReference), retry);
//      }

      return null;

    } catch (Exception e) {
      throw new InternalServerException();
    }
  }

  public ServiceFunctionUpdateCommand continuationReference(ContinuationReference continuationReference) {
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
  public ServiceFunctionUpdateCommand scriptExecutionId(String scriptExecutionId) {
    this.scriptExecutionId = scriptExecutionId;
    return this;
  }

  public String getExecutionId() {
    return this.executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public ServiceFunctionUpdateCommand executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public String getLevel() {
    return this.level;
  }
  public void setLevel(String level) {
    this.level = level;
  }
  public ServiceFunctionUpdateCommand level(String level) {
    this.level = level;
    return this;
  }
  public ServiceFunctionUpdateCommand levelInfo() {
    this.level = LEVEL_INFO;
    return this;
  }
  public ServiceFunctionUpdateCommand levelWarning() {
    this.level = LEVEL_WARNING;
    return this;
  }
  public ServiceFunctionUpdateCommand levelError() {
    this.level = LEVEL_ERROR;
    return this;
  }

  public String getMessage() {
    return this.message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  public ServiceFunctionUpdateCommand message(String message) {
    this.message = message;
    return this;
  }

  public Instant getRetry() {
    return this.retry;
  }
  public void setRetry(Instant retry) {
    this.retry = retry;
  }
  public ServiceFunctionUpdateCommand retry(Instant retry) {
    this.retry = retry;
    return this;
  }
}

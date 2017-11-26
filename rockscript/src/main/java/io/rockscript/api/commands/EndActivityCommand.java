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

import io.rockscript.Engine;
import io.rockscript.api.Command;
import io.rockscript.api.Doc;
import io.rockscript.engine.impl.ContinuationReference;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.http.servlet.BadRequestException;
import io.rockscript.http.servlet.InternalServerException;

import java.util.LinkedHashMap;
import java.util.Map;

public class EndActivityCommand implements Command<ScriptExecutionResponse> {

  protected String scriptExecutionId;
  protected String executionId;
  protected Object result;

  @Override
  public ScriptExecutionResponse execute(Engine engine) {
    BadRequestException.throwIfNull(scriptExecutionId, "scriptExecutionId is a mandatory field");
    BadRequestException.throwIfNull(scriptExecutionId, "executionId is a mandatory field");
    try {
      ContinuationReference continuationReference = new ContinuationReference(scriptExecutionId, executionId);
      EngineScriptExecution engineScriptExecution = engine
          .getScriptRunner()
          .endActivity(continuationReference, result);
      return new ScriptExecutionResponse(engineScriptExecution);

    } catch (Exception e) {
      throw new InternalServerException();
    }
  }

  @Override
  public Doc getDoc() {
    return new Doc()
      .type("endActivity")
      .label("End activity")
      .content("TODO");
  }

  public EndActivityCommand continuationReference(ContinuationReference continuationReference) {
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
  public EndActivityCommand scriptExecutionId(String scriptExecutionId) {
    this.scriptExecutionId = scriptExecutionId;
    return this;
  }

  public String getExecutionId() {
    return this.executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public EndActivityCommand executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public Object getResult() {
    return this.result;
  }
  public void setResult(Object result) {
    this.result = result;
  }
  public EndActivityCommand result(Object result) {
    this.result = result;
    return this;
  }

  public EndActivityCommand resultProperty(String propertyName, Object propertyValue) {
    if (result==null) {
      result = new LinkedHashMap<String,Object>();
    }
    if (!(result instanceof Map)) {
      throw new RuntimeException("resultProperty can only be used with maps / script objects");
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> resultMap = (Map<String, Object>) this.result;
    resultMap.put(propertyName, propertyValue);
    return this;
  }
}

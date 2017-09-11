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
package io.rockscript.engine;

import io.rockscript.engine.impl.ContinuationReference;
import io.rockscript.engine.impl.EngineScriptExecution;

import java.util.LinkedHashMap;
import java.util.Map;

public class EndActivityCommand extends CommandImpl<EngineEndActivityResponse> {

  protected String scriptExecutionId;
  protected String executionId;
  protected Object result;

  public EndActivityCommand() {
  }

  public EndActivityCommand(Configuration configuration) {
    super(configuration);
  }

  @Override
  protected EngineEndActivityResponse execute(Configuration configuration) {
    ContinuationReference continuationReference = new ContinuationReference(scriptExecutionId, executionId);
    EngineScriptExecution engineScriptExecution = configuration
        .getEngine()
        .endActivity(continuationReference, result);
    return new EngineEndActivityResponse(engineScriptExecution);
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

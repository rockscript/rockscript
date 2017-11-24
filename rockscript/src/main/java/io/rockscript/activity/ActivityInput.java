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
package io.rockscript.activity;

import com.google.gson.Gson;
import io.rockscript.Engine;
import io.rockscript.engine.impl.ContinuationReference;
import io.rockscript.engine.impl.Execution;
import io.rockscript.engine.impl.Location;
import io.rockscript.engine.impl.ScriptRunner;
import io.rockscript.http.client.Http;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@SuppressWarnings("unchecked")
public class ActivityInput {

  ContinuationReference continuationReference;
  String scriptExecutionId;
  String executionId;
  List<Object> args;

  transient Execution<?> execution;

  public ActivityInput(Execution<?> execution, List<Object> args) {
    this.scriptExecutionId = execution.getScriptExecution().getId();
    this.executionId = execution.getId();
    this.continuationReference = new ContinuationReference(
        execution.getScriptExecution().getId(),
        execution.getId()
    );
    this.args = args;
    this.execution = execution;
  }

  public String getScriptExecutionId() {
    return scriptExecutionId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public ContinuationReference getContinuationReference() {
    return continuationReference;
  }

  public List<Object> getArgs() {
    return args;
  }

  public Engine getEngine() {
    return execution.getEngineScript().getEngine();
  }

  /** Convenience method to get the argument by index. */
  public <T> T getArg(int index) {
    return args!=null ? (T) args.get(index) : null;
  }

  /** Convenience method to extract a json property from the first
   * json object argument. */
  public <T> T getArgProperty(String propertyName) {
    if (args==null) {
      return null;
    }
    Map<String,Object> objectArg = (Map<String, Object>) args.get(0);
    return (T) objectArg.get(propertyName);
  }

  public Gson getGson() {
    return getEngine().getGson();
  }

  public ScriptRunner getScriptRunner() {
    return getEngine().getScriptRunner();
  }

  public Location getElementLocation() {
    return execution.getElement().getLocation();
  }

  public Executor getExecutor() {
    return getEngine().getExecutor();
  }

  public Execution<?> getExecution() {
    return execution;
  }

  public Http getHttp() {
    return getEngine().getHttp();
  }
}

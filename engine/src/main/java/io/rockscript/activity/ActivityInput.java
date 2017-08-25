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

import io.rockscript.activity.http.ActivityContext;
import io.rockscript.engine.Execution;

import java.util.List;
import java.util.Map;

public class ActivityInput {

  String scriptExecutionId;
  String executionId;
  List<Object> args;

  // activityContext is transient because it should not be serialized with Gson
  transient ActivityContext activityContext;

  public ActivityInput(Execution<?> execution, List<Object> args) {
    this.scriptExecutionId = execution.getScriptExecution().getId();
    this.executionId = execution.getId();
    this.args = args;
    this.activityContext = execution.getScript().getConfiguration();
  }

  public String getScriptExecutionId() {
    return scriptExecutionId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public List<Object> getArgs() {
    return args;
  }

  public Object getArg(int index) {
    return args!=null ? args.get(index) : null;
  }

  public ActivityContext getActivityContext() {
    return activityContext;
  }

  public void setActivityContext(ActivityContext activityContext) {
    this.activityContext = activityContext;
  }

  /** Convenience method to extract a json property from the first
   * json object argument. */
  public <T> T getArgProperty(String propertyName) {
    Map<String,Object> objectArg = (Map<String, Object>) args.get(0);
    return (T) objectArg.get(propertyName);
  }
}

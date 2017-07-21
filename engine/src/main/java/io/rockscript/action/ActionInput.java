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
package io.rockscript.action;

import java.util.List;

public class ActionInput {

  public final String scriptExecutionId;
  public final String executionId;
  public final List<Object> args;

  // TODO Combine scriptExecutionId and executionId into a ScriptContext parameter.
  public ActionInput(String scriptExecutionId, String executionId, List<Object> args) {
    this.scriptExecutionId = scriptExecutionId;
    this.executionId = executionId;
    this.args = args;
  }
}

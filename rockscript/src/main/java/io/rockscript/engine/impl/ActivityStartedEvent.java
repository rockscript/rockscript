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

import io.rockscript.engine.EngineException;

import java.util.List;

public class ActivityStartedEvent extends ExecutableEvent<ArgumentsExpressionExecution> {

  String activityName;
  List<Object> args;

  /** constructor for gson serialization */
  ActivityStartedEvent() {
  }

  public ActivityStartedEvent(ArgumentsExpressionExecution argumentsExpressionExecution) {
    super(argumentsExpressionExecution);
    if (argumentsExpressionExecution.activity==null) {
      throw new EngineException("Activity doesn't exist: "+argumentsExpressionExecution.element.getText());
    }
    this.activityName = getActivityName(argumentsExpressionExecution);
    this.args = argumentsExpressionExecution.args;
  }

  @Override
  public void execute(ArgumentsExpressionExecution execution) {
    execution.startActivityExecute();
  }

  static String getActivityName(ArgumentsExpressionExecution argumentsExpressionExecution) {
    return argumentsExpressionExecution.activity.toString();
  }

  @Override
  public String toString() {
    return "[" + scriptExecutionId + "|" + executionId + "] " +
        "Started [" +
        activityName +
        "]"+
        (args!=null ? " with args "+args.toString() : " without args");
  }
}

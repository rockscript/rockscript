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

import io.rockscript.activity.Activity;
import io.rockscript.activity.ActivityInput;
import io.rockscript.activity.ActivityOutput;
import io.rockscript.engine.ActivityContinuation;
import io.rockscript.engine.job.RetryPolicy;
import io.rockscript.engine.job.impl.ActivityRetryAfterError;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;

public class ArgumentsExpressionExecution extends Execution<ArgumentsExpression> {

  Activity activity = null;
  List<Object> args = null;
  boolean ended = false;

  public ArgumentsExpressionExecution(ArgumentsExpression element, Execution parent) {
    super(parent.createInternalExecutionId(), element, parent);
  }

  @Override
  public void start() {
    startChild(element.getFunctionExpression());
  }

  @Override
  public void childEnded(Execution child) {
    startNextParameter();
  }

  private void startNextParameter() {
    int parameterIndex = children.size()-1; // -1 because the first one is the function expression
    List<SingleExpression> parameters = element.getArgumentExpressions();
    if (parameterIndex < parameters.size()) {
      ScriptElement piece = parameters.get(parameterIndex);
      startChild(piece);
    } else {
      Execution functionExpressionExecution = children.get(0);
      this.activity = (Activity) functionExpressionExecution.getResult();
      this.args = collectArgsFromChildren();

      if (activity instanceof SystemImportActivity) {
        invokeSystemImportFunction();
      } else {
        startActivity();
      }
    }
  }

  private List<Object> collectArgsFromChildren() {
    List<Object> args = new ArrayList<>();
    List<Execution> argExecutions = children.subList(1, children.size());
    for (Execution argExecution: argExecutions) {
      args.add(argExecution.getResult());
    }
    return args;
  }

  private void invokeSystemImportFunction() {
    // import functions have to be re-executed when the events
    // are applied because they can return functions
    ActivityOutput output = startActivityInvoke();
    Object importedObject = output.getResult();
    // dispatch(new ObjectImportedEvent(this, importedObject));
    endActivityExecute(importedObject);
  }

  private void startActivity() {
    dispatchAndExecute(new ActivityStartedEvent(this));
  }

  public void startActivityExecute() {
    EngineScriptExecution scriptExecution = getScriptExecution();
    ExecutionMode executionMode = scriptExecution.getExecutionMode();
    if (executionMode!=ExecutionMode.REBUILDING) {
      ActivityOutput activityOutput = null;
      try {
        activityOutput = startActivityInvoke();
      } catch (Exception e) {
        handleActivityError(e.getMessage(), scriptExecution, null);
      }
      if (activityOutput!=null) {
        if (activityOutput.isEnded()) {
          endActivity(activityOutput.getResult());

        } else if (activityOutput.isError()) {
          RetryPolicy retryPolicy = activityOutput.getRetryPolicy();
          String errorMessage = activityOutput.getError();
          handleActivityError(errorMessage, scriptExecution, retryPolicy);

        } else {
          dispatch(new ActivityWaitingEvent(this));
        }
      }
    }
  }

  private void handleActivityError(String errorMessage, EngineScriptExecution scriptExecution, RetryPolicy retryPolicy) {
    TemporalAmount retryDelay = retryPolicy!=null ? retryPolicy.removeFirst() : null;
    Instant retryTime = retryDelay!=null ? Instant.now().plus(retryDelay) : null;
    scriptExecution.errorEvent = new ActivityStartErrorEvent(this, errorMessage, retryTime);
    dispatch(scriptExecution.errorEvent);
    if (retryTime!=null) {
      getConfiguration().getJobService().schedule(
        new ActivityRetryAfterError(this),
        retryTime,
        retryPolicy
      );
    }
  }

  public void endActivity(Object result) {
    dispatchAndExecute(new ActivityEndedEvent(this, result));
    // Continues at this.endActivityExecute()
  }

  // Continuation from endActivity -> ActivityEndedEvent
  void endActivityExecute(Object result) {
    setResult(result);
    ended = true;
    end();
  }

  public ActivityOutput startActivityInvoke() {
    ActivityInput activityInput = new ActivityInput(this, args);
    try {
      return activity.invoke(activityInput);
    } catch (Exception e) {
      return ActivityOutput.error(e.getMessage());
    }
  }

  public ActivityContinuation getActivityContinuation() {
    return !ended ? new ActivityContinuation(id, activity.toString(), args) : null;
  }
}

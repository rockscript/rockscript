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

import io.rockscript.engine.ServiceFunctionContinuation;
import io.rockscript.service.ServiceFunction;
import io.rockscript.service.ServiceFunctionInput;
import io.rockscript.service.ServiceFunctionOutput;
import io.rockscript.engine.job.RetryPolicy;
import io.rockscript.engine.job.RetryServiceFunctionJobHandler;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.List;

public class ArgumentsExpressionExecution extends Execution<ArgumentsExpression> {

  ServiceFunction serviceFunction = null;
  List<Object> args = null;
  boolean ended = false;
  int failedAttemptsCount = 0;

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
    if (parameters!=null && parameterIndex < parameters.size()) {
      ScriptElement piece = parameters.get(parameterIndex);
      startChild(piece);
    } else {
      Execution functionExpressionExecution = children.get(0);
      this.serviceFunction = (ServiceFunction) functionExpressionExecution.getResult();
      this.args = collectResultsFromChildren().subList(1, children.size());

      if (serviceFunction instanceof SystemImportServiceFunction) {
        invokeSystemImportFunction();
      } else {
        startFunction();
      }
    }
  }

  private void invokeSystemImportFunction() {
    // import functions have to be re-executed when the events
    // are applied because they can return functions
    ServiceFunctionOutput output = startFunctionInvoke();
    Object importedObject = output.getResult();
    // dispatch(new ObjectImportedEvent(this, importedObject));
    endFunctionExecute(importedObject);
  }

  private void startFunction() {
    dispatchAndExecute(new ServiceFunctionStartedEvent(this));
  }

  public void startFunctionExecute() {
    EngineScriptExecution scriptExecution = getScriptExecution();
    ExecutionMode executionMode = scriptExecution.getExecutionMode();
    if (executionMode!=ExecutionMode.REBUILDING) {
      ServiceFunctionOutput serviceFunctionOutput = null;
      try {
        serviceFunctionOutput = startFunctionInvoke();
      } catch (Exception e) {
        handleServiceFunctionError(e.getMessage(), scriptExecution, null);
      }
      if (serviceFunctionOutput!=null) {
        if (serviceFunctionOutput.isEnded()) {
          endFunction(serviceFunctionOutput.getResult());

        } else if (serviceFunctionOutput.isError()) {
          RetryPolicy retryPolicy = serviceFunctionOutput.getRetryPolicy();
          String errorMessage = serviceFunctionOutput.getError();
          handleServiceFunctionError(errorMessage, scriptExecution, retryPolicy);

        } else {
          dispatch(new ServiceFunctionWaitingEvent(this));
        }
      }
    }
  }

  private void handleServiceFunctionError(String errorMessage, EngineScriptExecution scriptExecution, RetryPolicy retryPolicy) {
    TemporalAmount retryDelay = retryPolicy!=null ? retryPolicy.get(failedAttemptsCount) : null;
    Instant retryTime = retryDelay!=null ? Instant.now().plus(retryDelay) : null;
    scriptExecution.errorEvent = new ServiceFunctionStartErrorEvent(this, errorMessage, retryTime);
    dispatchAndExecute(scriptExecution.errorEvent);
    if (retryTime!=null) {
      getConfiguration().getJobService().schedule(
        new RetryServiceFunctionJobHandler(this),
        retryTime,
        retryPolicy
      );
    }
  }

  public void endFunction(Object result) {
    dispatchAndExecute(new ServiceFunctionEndedEvent(this, result));
    // Continues at this.endFunctionExecute()
  }

  // Continuation from endFunction -> ServiceFunctionEndedEvent
  void endFunctionExecute(Object result) {
    setResult(result);
    ended = true;
    end();
  }

  public ServiceFunctionOutput startFunctionInvoke() {
    ServiceFunctionInput input = new ServiceFunctionInput(this, args);
    try {
      return serviceFunction.invoke(input);
    } catch (Exception e) {
      return ServiceFunctionOutput.error(e.getMessage());
    }
  }

  public ServiceFunctionContinuation getServiceFunctionContinuation() {
    return !ended ? new ServiceFunctionContinuation(id, serviceFunction.toString(), args) : null;
  }
}

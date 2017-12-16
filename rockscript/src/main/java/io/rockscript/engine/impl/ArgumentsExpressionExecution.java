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
import io.rockscript.engine.job.RetryServiceFunctionJobHandler;
import io.rockscript.service.ServiceFunction;
import io.rockscript.service.ServiceFunctionInput;
import io.rockscript.service.ServiceFunctionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

public class ArgumentsExpressionExecution extends Execution<ArgumentsExpression> {

  static Logger log = LoggerFactory.getLogger(ArgumentsExpressionExecution.class);

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
    int parameterIndex = children.size() - 1; // -1 because the first one is the function expression
    List<SingleExpression> parameters = element.getArgumentExpressions();
    if (parameters!=null && parameterIndex<parameters.size()) {
      ScriptElement piece = parameters.get(parameterIndex);
      startChild(piece);
    } else {
      Execution functionExpressionExecution = children.get(0);
      this.serviceFunction = (ServiceFunction) functionExpressionExecution.getResult();
      this.args = collectResultsFromChildren().subList(1, children.size());

      // TODO create separate mechanism for non-service functions
      if (serviceFunction instanceof SystemImportServiceFunction
          || serviceFunction instanceof EncodeUriFunction) {
        invokeSystemFunction();
      } else {
        startServiceFunction();
      }
    }
  }

  private void invokeSystemFunction() {
    // import functions have to be re-executed when the events
    // are applied because they can return functions
    ServiceFunctionOutput output = startFunctionInvoke();
    Object importedObject = output.getResult();
    // dispatch(new ObjectImportedEvent(this, importedObject));
    endFunctionExecute(importedObject);
  }

  private void startServiceFunction() {
    dispatch(new ServiceFunctionStartingEvent(this));
    startFunctionExecute();
  }

  public void startFunctionExecute() {
    EngineScriptExecution scriptExecution = getScriptExecution();
    ExecutionMode executionMode = scriptExecution.getExecutionMode();
    if (executionMode!=ExecutionMode.REPLAYING) {
      ServiceFunctionOutput serviceFunctionOutput = null;
      try {
        serviceFunctionOutput = startFunctionInvoke();
      } catch (Exception e) {
        // TODO transform this to an Engine.engineLogStore notification because it
        // should be considered a bug if a service function throws an error.
        // ServiceFunction's should use serviceFunctionOutput.isError() to indicate errors.
        handleServiceFunctionError(e.getMessage(), null);
      }
      if (serviceFunctionOutput!=null) {
        if (serviceFunctionOutput.isEnded()) {
          endFunction(serviceFunctionOutput.getResult());

        } else if (serviceFunctionOutput.isError()) {
          Instant retryTime = serviceFunctionOutput.getRetryTime();
          String errorMessage = serviceFunctionOutput.getError();
          handleServiceFunctionError(errorMessage, retryTime);

        } else {
          dispatch(new ServiceFunctionWaitingEvent(this));
        }
      }
    }
  }

  public void handleServiceFunctionError(String errorMessage, Instant retryTime) {
    EngineScriptExecution scriptExecution = getScriptExecution();
    scriptExecution.errorEvent = new ServiceFunctionErrorEvent(this, errorMessage, retryTime);
    dispatchAndExecute(scriptExecution.errorEvent);
    if (retryTime!=null) {
      getEngine().getJobService().schedule(
        new RetryServiceFunctionJobHandler(this),
        retryTime);
    }
  }

  public void retry() {
    dispatch(new ServiceFunctionRetryingEvent(this));
    startFunctionExecute();
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
      log.debug("Service function error: "+e.getMessage(), e);
      return ServiceFunctionOutput.error(e.getMessage());
    }
  }

  public ServiceFunctionContinuation getServiceFunctionContinuation() {
    return !ended ? new ServiceFunctionContinuation(id, serviceFunction.toString(), args) : null;
  }

  public int getFailedAttemptsCount() {
    return this.failedAttemptsCount;
  }
  public void setFailedAttemptsCount(int failedAttemptsCount) {
    this.failedAttemptsCount = failedAttemptsCount;
  }

  public void incrementFailedAttemptsCount() {
    failedAttemptsCount++;
  }
}


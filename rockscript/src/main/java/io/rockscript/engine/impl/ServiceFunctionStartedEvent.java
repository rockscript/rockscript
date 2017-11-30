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

import io.rockscript.service.ServiceFunction;
import io.rockscript.engine.EngineException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceFunctionStartedEvent extends ExecutableEvent<ArgumentsExpressionExecution> {

  String serviceName;
  String functionName;
  Object args;

  /** constructor for gson serialization */
  ServiceFunctionStartedEvent() {
  }

  @SuppressWarnings("unchecked")
  public ServiceFunctionStartedEvent(ArgumentsExpressionExecution argumentsExpressionExecution) {
    super(argumentsExpressionExecution);
    if (argumentsExpressionExecution.serviceFunction==null) {
      throw new EngineException("ServiceFunction doesn't exist: "+argumentsExpressionExecution.element.getText());
    }

    ServiceFunction serviceFunction = argumentsExpressionExecution.serviceFunction;
    this.serviceName = serviceFunction.getServiceName();
    this.functionName = serviceFunction.getFunctionName();

    List<String> argNames = serviceFunction.getArgNames();
    if (argNames==null
      && argumentsExpressionExecution.args!=null
      && argumentsExpressionExecution.args.size()==1
      && argumentsExpressionExecution.args.get(0) instanceof Map) {
      this.args = (Map<String, Object>) argumentsExpressionExecution.args.get(0);
    } else {
      if (argumentsExpressionExecution.args!=null) {
        if (argNames!=null) {
          Map<String,Object> argsMap = new LinkedHashMap<>();
          for (int i=0; i<argumentsExpressionExecution.args.size(); i++) {
            Object argValue = null;
            if (argumentsExpressionExecution.args.size() > i) {
              argValue = argumentsExpressionExecution.args.get(i);
            }
            String argName = argNames!=null && argNames.size() > 1 ? argNames.get(i) : "arg"+i;
            argsMap.put(argName, argValue);
          }
          this.args = argsMap;

        } else {
          this.args = argumentsExpressionExecution.args;
        }
      }
    }
  }

  @Override
  public void execute(ArgumentsExpressionExecution execution) {
    execution.startFunctionExecute();
  }

  @Override
  public String toString() {
    return "[" + scriptExecutionId + "|" + executionId + "] " +
        "Started [" +
           functionName +
        "]"+
        (args!=null ? " with args "+args.toString() : " without args");
  }
}

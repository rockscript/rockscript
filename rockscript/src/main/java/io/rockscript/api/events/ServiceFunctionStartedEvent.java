/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.rockscript.api.events;

import io.rockscript.engine.impl.ArgumentsExpressionExecution;
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
    if (argumentsExpressionExecution.getServiceFunction()==null) {
      throw new EngineException("ServiceFunction doesn't exist: "+argumentsExpressionExecution.getElement().getText());
    }

    ServiceFunction serviceFunction = argumentsExpressionExecution.getServiceFunction();
    this.serviceName = serviceFunction.getServiceName();
    this.functionName = serviceFunction.getFunctionName();

    List<String> argNames = serviceFunction.getArgNames();
    List<Object> args = argumentsExpressionExecution.getArgs();
    if (argNames==null
        && args!=null
        && args.size()==1
        && args.get(0) instanceof Map) {
      this.args = (Map<String, Object>) args.get(0);
    } else {
      if (args!=null) {
        if (argNames!=null) {
          Map<String,Object> argsMap = new LinkedHashMap<>();
          for (int i = 0; i<args.size(); i++) {
            Object argValue = null;
            if (args.size()>i) {
              argValue = args.get(i);
            }
            String argName = argNames!=null && argNames.size() > 1 ? argNames.get(i) : "arg"+i;
            argsMap.put(argName, argValue);
          }
          this.args = argsMap;

        } else {
          this.args = args;
        }
      }
    }
  }

  @Override
  public boolean isRecoverable() {
    return true;
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

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

public class ServiceFunctionEndedEvent extends ExecutableEvent<ArgumentsExpressionExecution> {

  Object result;

  /** constructor for gson serialization */
  ServiceFunctionEndedEvent() {
  }

  public ServiceFunctionEndedEvent(ArgumentsExpressionExecution argumentsExpressionExecution, Object result) {
    super(argumentsExpressionExecution);
    this.result = result;
  }

  @Override
  public void execute(ArgumentsExpressionExecution execution) {
    execution.endFunctionExecute(result);
  }

  @Override
  public boolean isReplay() {
    return true;
  }

  @Override
  public String toString() {
    return "[" + scriptExecutionId + "|" + executionId + "] " +
        "Received callback" +
        (result!=null ? " with result "+result : " without result");
  }
}

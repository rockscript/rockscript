/*
 * Copyright (c) 2018 RockScript.io.
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
package io.rockscript.engine.impl;

public class IfExecution extends Execution<IfStatement> {

  public IfExecution(String internalExecutionId, IfStatement ifStatement, Execution parent) {
    super(internalExecutionId, ifStatement, parent);
  }

  @Override
  public void start() {
    startChild(element.getConditionExpression());
  }

  @Override
  public void childEnded(Execution child) {
    if (children.size()==1) {
      Object conditionResult = children.get(0).getResult();
      Converter converter = getEngine().getConverter();
      Boolean conditionResultBoolean = converter.toBoolean(conditionResult);
      if (Boolean.TRUE.equals(conditionResultBoolean)) {
        startChild(element.getThenStatement());
      } else if (element.getElseStatement()!=null) {
        startChild(element.getElseStatement());
      } else {
        // there is no else
        end();
      }
    } else {
      // the then or the else statements have ended
      end();
    }
  }
}

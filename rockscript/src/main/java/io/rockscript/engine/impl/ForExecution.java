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

import java.util.List;

public class ForExecution extends Execution<ForStatement> {

  int nextVarDeclarationIndex = 0;

  public ForExecution(String internalExecutionId, ForStatement forStatement, Execution parent) {
    super(internalExecutionId, forStatement, parent);
  }

  @Override
  public void start() {
    startChild(element.getVariableDeclarations());
  }

  @Override
  public void childEnded(Execution child) {
    if (child.getElement()==element.getVariableDeclarations()) {
      // start evaluating the while condition
      startChild(element.getWhileConditions());
    } else if (child.getElement()==element.getWhileConditions()) {
      if (isLastEvaluatedConditionTrue()) {
        // start the iterative statement
        startChild(element.getIterativeStatement());
      } else {
        end();
      }
    } else if (child.getElement()==element.getIterativeStatement()) {
      startChild(element.getIncrements());
    } else if (child.getElement()==element.getIncrements()) {
      startChild(element.getWhileConditions());
    } else {
      throw new RuntimeException("huh?!");
    }
  }

  public boolean isLastEvaluatedConditionTrue() {
    List<Execution> children = getChildren();
    Execution lastChild = children!=null && !children.isEmpty() ? children.get(children.size()-1) : null;
    if (lastChild.getElement()!=element.getWhileConditions()) {
      throw new RuntimeException("huh?!");
    }
    Converter converter = getEngine().getConverter();
    return converter.toBoolean(lastChild.getResult());
  }
}

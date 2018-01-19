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

import io.rockscript.util.Lists;

import java.util.List;

public class IfStatement extends Statement {

  SingleExpression conditionExpression;
  Statement thenStatement;
  Statement elseStatement;

  public IfStatement(Integer index, Location location, SingleExpression conditionExpression, Statement thenStatement, Statement elseStatement) {
    super(index, location);
    this.conditionExpression = conditionExpression;
    this.thenStatement = thenStatement;
    this.elseStatement = elseStatement;
  }

  @Override
  public Execution createExecution(Execution parent) {
    return new IfExecution(parent.createInternalExecutionId(), this, parent);
  }

  @Override
  protected List<? extends ScriptElement> getChildren() {
    List<ScriptElement> children = Lists.arrayList(conditionExpression, thenStatement);
    if (elseStatement!=null) {
      children.add(elseStatement);
    }
    return children;
  }

  public SingleExpression getConditionExpression() {
    return conditionExpression;
  }

  public Statement getThenStatement() {
    return thenStatement;
  }

  public Statement getElseStatement() {
    return elseStatement;
  }
}

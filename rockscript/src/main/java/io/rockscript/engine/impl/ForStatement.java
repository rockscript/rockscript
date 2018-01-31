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

import java.util.ArrayList;
import java.util.List;

public class ForStatement extends Statement {

  VariableDeclarationList variableDeclarations;
  SingleExpressionList whileConditions;
  SingleExpressionList increments;
  Statement iterativeStatement;

  public ForStatement(Integer index, Location location, VariableDeclarationList variableDeclarations, SingleExpressionList whileConditions, SingleExpressionList increments, Statement iterativeStatement) {
    super(index, location);
    this.variableDeclarations = variableDeclarations;
    this.whileConditions = whileConditions;
    this.increments = increments;
    this.iterativeStatement = iterativeStatement;
  }

  @Override
  public Execution createExecution(Execution parent) {
    return new ForExecution(parent.createInternalExecutionId(), this, parent);
  }

  @Override
  protected List<? extends ScriptElement> getChildren() {
    List<ScriptElement> children = new ArrayList<>();
    children.addAll(variableDeclarations.getChildren());
    children.addAll(whileConditions.getChildren());
    children.addAll(increments.getChildren());
    children.add(iterativeStatement);
    return children;
  }

  public Statement getVariableDeclarations() {
    return variableDeclarations;
  }

  public SingleExpressionList getWhileConditions() {
    return whileConditions;
  }

  public SingleExpressionList getIncrements() {
    return increments;
  }

  public Statement getIterativeStatement() {
    return iterativeStatement;
  }
}

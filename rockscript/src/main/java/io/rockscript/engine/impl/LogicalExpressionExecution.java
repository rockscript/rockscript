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

import static io.rockscript.engine.impl.Converter.*;
import static io.rockscript.engine.impl.EqualityExpressionExecution.checkValidValue;

public class LogicalExpressionExecution extends Execution<LogicalExpression> {

  public static final String OPERATOR_AND = "&&";
  public static final String OPERATOR_OR = "||";
  public static final String OPERATOR_NOT = "!";

  public LogicalExpressionExecution(LogicalExpression logicalExpression, Execution parent) {
    super(parent.createInternalExecutionId(), logicalExpression, parent);
  }

  @Override
  public void start() {
    startChild(element.getLeft());
  }

  @Override
  public void childEnded(Execution child) {
    String operator = element.getOperator();
    if (children.size()==1 && operator!=OPERATOR_NOT) {
      startChild(element.getRight());
    } else {
      Converter converter = getEngine().getConverter();

      Object leftValue = checkValidValue("left", getChildren().get(0).getResult());
      boolean leftBoolean = converter.toBoolean(leftValue);

      Object rightValue = null;
      boolean rightBoolean = false;

      if (operator!=OPERATOR_NOT) {
        rightValue = checkValidValue("right", getChildren().get(1).getResult());
        rightBoolean = converter.toBoolean(rightValue);
      }

      if (OPERATOR_AND.equals(operator)) {
        setResult(leftBoolean && rightBoolean);
      } else if (OPERATOR_OR.equals(operator)) {
        setResult(leftBoolean || rightBoolean);
      } else if (OPERATOR_NOT.equals(operator)) {
        setResult(!leftBoolean);
      } else {
        throw new UnsupportedOperationException("Invalid operator: "+operator);
      }

      end();
    }
  }
}

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

import io.rockscript.api.events.VariableCreatedEvent;

public class UnaryArithmaticExpressionExecution extends Execution<UnaryArithmaticExpression> {

  public static final String OPERATOR_PLUSPLUS = "++";
  public static final String OPERATOR_MINUSMINUS = "--";
  public static final String RESULT_CAPTURE_POST = "post";
  public static final String RESULT_CAPTURE_PRE = "pre";

  public UnaryArithmaticExpressionExecution(UnaryArithmaticExpression element, Execution parent) {
    super(parent.createInternalExecutionId(), element, parent);
  }

  @Override
  public void start() {
    startChild(element.getExpression());
  }

  public void childEnded(Execution child) {
    Assignable assignable = (Assignable) children.get(0);
    Object value = children.get(0).getResult();

    Converter converter = getEngine().getConverter();
    String resultCapture = element.getResultCapture();
    String operator = element.getOperator();

    Number number = converter.toNumber(value);
    if (number==null) {
      throw new RuntimeException(operator+" must be applied to a number.  Not "+value+(value!=null?" ("+value.getClass().getSimpleName()+")":""));
    }
    double d = (double) number.doubleValue();
    if (resultCapture==RESULT_CAPTURE_POST) {
      setResult(d);
    }
    if (operator==OPERATOR_PLUSPLUS) {
      d++;
    } else if (operator==OPERATOR_MINUSMINUS) {
      d--;
    } else {
      throw new RuntimeException("Unknown operator "+operator);
    }
    if (resultCapture==RESULT_CAPTURE_PRE) {
      setResult(d);
    }
    assignable.assign(d);
    end();
  }
}

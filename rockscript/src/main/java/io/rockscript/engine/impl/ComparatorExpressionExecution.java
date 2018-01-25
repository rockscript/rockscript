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

import java.util.Comparator;

import static io.rockscript.engine.impl.Converter.*;
import static io.rockscript.engine.impl.EqualityExpressionExecution.checkValidValue;

public class ComparatorExpressionExecution extends Execution<ComparatorExpression> {

  String comparator;

  public ComparatorExpressionExecution(ComparatorExpression comparatorExpression, Execution parent, String comparator) {
    super(parent.createInternalExecutionId(), comparatorExpression, parent);
    this.comparator = comparator;
  }

  @Override
  public void start() {
    startChild(element.getLeft());
  }

  public static boolean supportsComparator(String comparator) {
    return "<".equals(comparator)
           || ">".equals(comparator)
           || "<=".equals(comparator)
           || ">=".equals(comparator);
  }

  @Override
  public void childEnded(Execution child) {
    if (children.size()==1) {
      startChild(element.getRight());
    } else {
      Object leftValue = checkValidValue("left", getChildren().get(0).getResult());
      Object rightValue = checkValidValue("right", getChildren().get(1).getResult());

      setResult(compare(leftValue, rightValue));

      end();
    }
  }

  private boolean compare(Object leftValue, Object rightValue) {
    Converter converter = getEngine().getConverter();

    if (isObject(leftValue) || isArray(leftValue)) {
      leftValue = converter.toPrimitiveDefault(leftValue);
    }

    if (isObject(rightValue) || isArray(rightValue)) {
      rightValue = converter.toPrimitiveDefault(rightValue);
    }

    if (isNumber(leftValue)
        || isNumber(rightValue)
        || isBoolean(leftValue)
        || isBoolean(rightValue)) {
      Number leftNumber = converter.toNumber(leftValue);
      Number rightNumber = converter.toNumber(rightValue);
      if (leftNumber==null || rightNumber==null) {
        return false;
      }
      if ("<".equals(comparator)) {
        return leftNumber.doubleValue() < rightNumber.doubleValue();
      } else if (">".equals(comparator)) {
        return leftNumber.doubleValue() > rightNumber.doubleValue();
      } else if ("<=".equals(comparator)) {
        return leftNumber.doubleValue() <= rightNumber.doubleValue();
      } else if (">=".equals(comparator)) {
        return leftNumber.doubleValue() >= rightNumber.doubleValue();
      }
    }

    if (isString(leftValue) || isString(rightValue)) {
      if (isNull(leftValue)
          ||isNull(rightValue)
          || isUndefined(leftValue)
          || isUndefined(rightValue)) {
        return false;
      }

      String leftString = converter.toString(leftValue);
      String rightString = converter.toString(rightValue);
      if ("<".equals(comparator)) {
        return converter.stringLessThan(leftString, rightString);
      } else if (">".equals(comparator)) {
        return converter.stringLessThan(rightString, leftString);
      } else if ("<=".equals(comparator)) {
        return converter.stringLessThan(leftString, rightString)
          || leftString.equals(rightString);
      } else if (">=".equals(comparator)) {
        return converter.stringLessThan(rightString, leftString)
               || leftString.equals(rightString);
      }
    }

    if (isNull(leftValue)
        && isNull(rightValue)
        && ("<=".equals(comparator) || (">=".equals(comparator)))) {
      return true;
    }

    return false;
  }

  private Double getDouble(Object number, Converter converter) {
    Number leftNumber = converter.toNumber(number);
    if (leftNumber!=null) {
      return leftNumber.doubleValue();
    } else {
      return 0d;
    }
  }
}

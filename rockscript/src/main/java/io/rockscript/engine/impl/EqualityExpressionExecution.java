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

import io.rockscript.engine.EngineException;

import java.util.Map;

public class EqualityExpressionExecution extends Execution<EqualityExpression> {

  String comparator;

  public EqualityExpressionExecution(EqualityExpression equalityExpression, Execution parent, String comparator) {
    super(parent.createInternalExecutionId(), equalityExpression, parent);
    this.comparator = comparator;
  }

  @Override
  public void start() {
    startChild(element.getLeft());
  }

  public static boolean supportsComparator(String comparator) {
    return "==".equals(comparator)
        || "===".equals(comparator);
  }

  @Override
  public void childEnded(Execution child) {
    if (children.size()==1) {
      startChild(element.getRight());
    } else {
      Object leftValue = checkValidValue(getChildren().get(0).getResult());
      Object rightValue = checkValidValue(getChildren().get(1).getResult());

      if ("==".equals(comparator)) {
        setResult(looseEquals(leftValue, rightValue));
      } else if ("===".equals(comparator)) {
        setResult(strictEquals(leftValue, rightValue));
      }

      end();
    }
  }

  private boolean looseEquals(Object leftValue, Object rightValue) {
    Converter converter = getEngine().getConverter();

    // See https://developer.mozilla.org/nl/docs/Web/JavaScript/Equality_comparisons_and_sameness

    if (leftValue==null || leftValue==Literal.UNDEFINED) {
      if (rightValue==null || rightValue==Literal.UNDEFINED) {
        return true;
      } else {
        return false;
      }
    } else if (leftValue instanceof Number) {
      if (rightValue instanceof Number) {
        return strictEquals(leftValue, rightValue);
      } else if (rightValue instanceof String) {
        return strictEquals(leftValue, converter.toNumber(rightValue));
      } else if (rightValue instanceof Boolean) {
        return strictEquals(leftValue, converter.toNumber(rightValue));
      } else if (rightValue instanceof Map) {
        return looseEquals(leftValue, converter.toPrimitive(rightValue));
      }
    } else if (leftValue instanceof String) {
      if (rightValue instanceof Number) {
        return strictEquals(converter.toNumber(leftValue), rightValue);
      } else if (rightValue instanceof String) {
        return strictEquals(leftValue, rightValue);
      } else if (rightValue instanceof Boolean) {
        return strictEquals(converter.toNumber(leftValue), converter.toNumber(rightValue));
      } else if (rightValue instanceof Map) {
        return looseEquals(leftValue, converter.toPrimitive(rightValue));
      }
    } else if (leftValue instanceof Boolean) {
      if (rightValue instanceof Number) {
        return strictEquals(converter.toNumber(leftValue), rightValue);
      } else if (rightValue instanceof String) {
        return strictEquals(converter.toNumber(leftValue), converter.toNumber(rightValue));
      } else if (rightValue instanceof Boolean) {
        return strictEquals(leftValue, rightValue);
      } else if (rightValue instanceof Map) {
        return looseEquals(converter.toNumber(leftValue), converter.toPrimitive(rightValue));
      }
    } else if (leftValue instanceof Map) {
      if (rightValue instanceof Number) {
        return strictEquals(converter.toNumber(leftValue), rightValue);
      } else if (rightValue instanceof String) {
        return strictEquals(converter.toNumber(leftValue), converter.toNumber(rightValue));
      } else if (rightValue instanceof Boolean) {
        return strictEquals(leftValue, rightValue);
      } else if (rightValue instanceof Map) {
        return objectEquals((Map<String,Object>)leftValue, (Map<String,Object>) rightValue);
      }
    }
    throw new EngineException("Bug 298347: please report this bug error code in a github issue");
  }

  private boolean strictEquals(Object leftValue, Object rightValue) {
    if ( (leftValue==null && rightValue==null)
         || (leftValue==Literal.UNDEFINED && rightValue==Literal.UNDEFINED) ) {
      return true;
    } else if (leftValue instanceof Number && rightValue instanceof Number) {
      return ((Number) leftValue).doubleValue()==((Number) rightValue).doubleValue();
    } else if (leftValue instanceof String && rightValue instanceof String) {
      return leftValue.equals(rightValue);
    } else if (leftValue instanceof Boolean && rightValue instanceof Boolean) {
      return leftValue.equals(rightValue);
    } else if (leftValue instanceof Map && rightValue instanceof Map) {
      return objectEquals((Map<String,Object>)leftValue, (Map<String,Object>) rightValue);
    }
    return false;
  }

  private boolean objectEquals(Map<String, Object> leftValue, Map<String, Object> rightValue) {
    if (leftValue==null || rightValue==null) {
      return false;
    }
    if (!leftValue.keySet().equals(rightValue.keySet())) {
      return false;
    }
    for (String property: leftValue.keySet()) {
      if (!strictEquals(leftValue.get(property), rightValue.get(property))) {
        return false;
      }
    }
    return true;
  }

  private Object checkValidValue(Object value) {
    if (!( value==null
           || value==Literal.UNDEFINED
           || value==Literal.NAN
           || value instanceof Number
           || value instanceof String
           || value instanceof Boolean
           || value instanceof Map )
      ) {
      throw new EngineException("Invalid right value: "+value+" ("+value.getClass().getName()+")");
    }
    return value;
  }

  private Object addObjects(Object leftValue, Object rightValue) {
    if (leftValue instanceof String) {
      return (String)leftValue + rightValue.toString();
    }
    throw new EngineException("Only string addition is supported atm", this);
  }
}

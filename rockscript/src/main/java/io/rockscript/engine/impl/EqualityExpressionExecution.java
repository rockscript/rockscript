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

import java.util.List;
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
      Object leftValue = checkValidValue("left", getChildren().get(0).getResult());
      Object rightValue = checkValidValue("right", getChildren().get(1).getResult());

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
      return (rightValue==null || rightValue==Literal.UNDEFINED);
    } else if (leftValue==Literal.NAN || rightValue==Literal.NAN) {
      return false;
    } else if (leftValue instanceof Number) {
      if (rightValue instanceof Number) {
        return strictEquals(leftValue, rightValue);
      } else if (rightValue instanceof String) {
        return strictEquals(leftValue, converter.toNumber(rightValue));
      } else if (rightValue instanceof Boolean) {
        return strictEquals(leftValue, converter.toNumber(rightValue));
      } else if (rightValue instanceof Map) {
        return looseEquals(leftValue, converter.toPrimitive(rightValue, "number"));
      } else if (rightValue instanceof List) {
        return false;
      } else if (rightValue==null || rightValue==Literal.UNDEFINED) {
        return false;
      }
    } else if (leftValue instanceof String) {
      if (rightValue instanceof Number) {
        return strictEquals(converter.toNumber(leftValue), rightValue);
      } else if (rightValue instanceof String) {
        return strictEquals(leftValue, rightValue);
      } else if (rightValue instanceof Boolean) {
        return strictEquals(converter.toNumber(leftValue), converter.toNumber(rightValue));
      } else if (rightValue instanceof Map) {
        return looseEquals(leftValue, converter.toPrimitive(rightValue, "string"));
      } else if (rightValue instanceof List) {
        return strictEquals(leftValue, converter.toString(rightValue));
      } else if (rightValue==null || rightValue==Literal.UNDEFINED) {
        return false;
      }
    } else if (leftValue instanceof Boolean) {
      if (rightValue instanceof Number) {
        return strictEquals(converter.toNumber(leftValue), rightValue);
      } else if (rightValue instanceof String) {
        return strictEquals(converter.toNumber(leftValue), converter.toNumber(rightValue));
      } else if (rightValue instanceof Boolean) {
        return strictEquals(leftValue, rightValue);
      } else if (rightValue instanceof Map) {
        return looseEquals(converter.toNumber(leftValue), converter.toPrimitive(rightValue, "number"));
      } else if (rightValue instanceof List) {
        return false;
      } else if (rightValue==null || rightValue==Literal.UNDEFINED) {
        return false;
      }
    } else if (leftValue instanceof Map) {
      if (rightValue instanceof Number) {
        return strictEquals(converter.toNumber(leftValue), rightValue);
      } else if (rightValue instanceof String) {
        return strictEquals(converter.toNumber(leftValue), converter.toNumber(rightValue));
      } else if (rightValue instanceof Boolean) {
        return strictEquals(leftValue, rightValue);
      } else if (rightValue instanceof Map) {
        return leftValue == rightValue;
      } else if (rightValue instanceof List) {
        return false;
      } else if (rightValue==null || rightValue==Literal.UNDEFINED) {
        return false;
      }
    } else if (leftValue instanceof List) {
      if (rightValue instanceof List) {
        return leftValue == rightValue;
      } else if (rightValue instanceof String) {
        return strictEquals(converter.toString(leftValue), rightValue);
      } else if (rightValue instanceof Number
                  || rightValue instanceof Boolean
                  || rightValue instanceof Map
                  || rightValue==null
                  || rightValue==Literal.UNDEFINED) {
        return false;
      }
    }
    throw new EngineException("Bug 298347: please report this bug error code in a github issue: left("+leftValue+"), right("+rightValue+")");
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
      return leftValue==rightValue;
    } else if (leftValue instanceof List && rightValue instanceof List) {
      return leftValue==rightValue;
    }
    return false;
  }

  private Object checkValidValue(String side, Object value) {
    if (!( value==null
           || value==Literal.UNDEFINED
           || value==Literal.NAN
           || value instanceof Number
           || value instanceof String
           || value instanceof Boolean
           || value instanceof Map
           || value instanceof List)
      ) {
      throw new EngineException("Invalid "+side+" value: "+value+" ("+value.getClass().getName()+")");
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

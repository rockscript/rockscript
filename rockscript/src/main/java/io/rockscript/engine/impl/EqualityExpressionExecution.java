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

import static com.sun.jmx.snmp.EnumRowStatus.isValidValue;
import static io.rockscript.engine.impl.Converter.*;

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
           || "===".equals(comparator)
           || "!=".equals(comparator)
           || "!==".equals(comparator);
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
      } else if ("!=".equals(comparator)) {
        setResult(!looseEquals(leftValue, rightValue));
      } else if ("!==".equals(comparator)) {
        setResult(!strictEquals(leftValue, rightValue));
      }

      end();
    }
  }

  private boolean looseEquals(Object leftValue, Object rightValue) {
    Converter converter = getEngine().getConverter();

    // See https://developer.mozilla.org/nl/docs/Web/JavaScript/Equality_comparisons_and_sameness

    if (isNull(leftValue) || isUndefined(leftValue)) {
      return (isNull(rightValue) || isUndefined(rightValue));
    } else if (isNaN(leftValue) || isNaN(rightValue)) {
      return false;
    } else if (isNumber(leftValue)) {
      if (isNumber(rightValue)) {
        return strictEquals(leftValue, rightValue);
      } else if (isString(rightValue)) {
        return strictEquals(leftValue, converter.toNumber(rightValue));
      } else if (isBoolean(rightValue)) {
        return strictEquals(leftValue, converter.toNumber(rightValue));
      } else if (isObject(rightValue)) {
        return looseEquals(leftValue, converter.toPrimitiveNumber(rightValue));
      } else if (isArray(rightValue)) {
        return false;
      } else if (isNull(rightValue) || isUndefined(rightValue)) {
        return false;
      }
    } else if (isString(leftValue)) {
      if (isNumber(rightValue)) {
        return strictEquals(converter.toNumber(leftValue), rightValue);
      } else if (isString(rightValue)) {
        return strictEquals(leftValue, rightValue);
      } else if (isBoolean(rightValue)) {
        return strictEquals(converter.toNumber(leftValue), converter.toNumber(rightValue));
      } else if (isObject(rightValue)) {
        return looseEquals(leftValue, converter.toPrimitiveString(rightValue));
      } else if (isArray(rightValue)) {
        return strictEquals(leftValue, converter.toString(rightValue));
      } else if (isNull(rightValue) || isUndefined(rightValue)) {
        return false;
      }
    } else if (leftValue instanceof Boolean) {
      if (isNumber(rightValue)) {
        return strictEquals(converter.toNumber(leftValue), rightValue);
      } else if (isString(rightValue)) {
        return strictEquals(converter.toNumber(leftValue), converter.toNumber(rightValue));
      } else if (isBoolean(rightValue)) {
        return strictEquals(leftValue, rightValue);
      } else if (isObject(rightValue)) {
        return looseEquals(converter.toNumber(leftValue), converter.toPrimitiveNumber(rightValue));
      } else if (isArray(rightValue)) {
        return false;
      } else if (isNull(rightValue) || isUndefined(rightValue)) {
        return false;
      }
    } else if (isObject(leftValue)) {
      if (isNumber(rightValue)) {
        return strictEquals(converter.toNumber(leftValue), rightValue);
      } else if (isString(rightValue)) {
        return looseEquals(converter.toPrimitiveString(leftValue), rightValue);
      } else if (isBoolean(rightValue)) {
        return strictEquals(leftValue, rightValue);
      } else if (isObject(rightValue)) {
        return leftValue == rightValue;
      } else if (isArray(rightValue)) {
        return false;
      } else if (isNull(rightValue) || isUndefined(rightValue)) {
        return false;
      }
    } else if (isArray(leftValue)) {
      if (isArray(rightValue)) {
        return leftValue == rightValue;
      } else if (isString(rightValue)) {
        return looseEquals(converter.toPrimitiveString(leftValue), rightValue);
      } else if (isNumber(rightValue)
                  || isBoolean(rightValue)
                  || isObject(rightValue)
                  || isNull(rightValue)
                 || isUndefined(rightValue)) {
        return false;
      }
    }
    throw new EngineException("Bug 298347: please report this bug error code in a github issue: left("+leftValue+"), right("+rightValue+")");
  }

  private boolean strictEquals(Object leftValue, Object rightValue) {
    if ( (isNull(leftValue) && isNull(rightValue))
         || (isUndefined(leftValue) && isUndefined(rightValue)) ) {
      return true;
    } else if (isNumber(leftValue) && isNumber(rightValue)) {
      return ((Number) leftValue).doubleValue()==((Number) rightValue).doubleValue();
    } else if (isString(leftValue) && isString(rightValue)) {
      return leftValue.equals(rightValue);
    } else if (isBoolean(leftValue) && isBoolean(rightValue)) {
      return leftValue.equals(rightValue);
    } else if (isObject(leftValue) && isObject(rightValue)) {
      return leftValue==rightValue;
    } else if (isArray(leftValue) && isArray(rightValue)) {
      return leftValue==rightValue;
    }
    return false;
  }

  public static Object checkValidValue(String side, Object value) {
    if (!isValid(value)) {
      throw new EngineException("Invalid "+side+" value: "+value+" ("+value.getClass().getName()+")");
    }
    return value;
  }

  private Object addObjects(Object leftValue, Object rightValue) {
    if (isString(leftValue)) {
      return (String)leftValue + rightValue.toString();
    }
    throw new EngineException("Only string addition is supported atm", this);
  }
}

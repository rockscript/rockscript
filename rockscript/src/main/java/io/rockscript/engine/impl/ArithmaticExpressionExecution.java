package io.rockscript.engine.impl;

import static io.rockscript.engine.impl.Converter.*;
import static io.rockscript.engine.impl.EqualityExpressionExecution.checkValidValue;

public class ArithmaticExpressionExecution extends Execution<ArithmaticExpression> {

  public ArithmaticExpressionExecution(ArithmaticExpression arithmaticExpression, Execution parent) {
    super(parent.createInternalExecutionId(), arithmaticExpression, parent);
  }

  @Override
  public void start() {
    startChild(element.getLeft());
  }

  @Override
  public void childEnded(Execution child) {
    if (children.size()==1) {
      startChild(element.getRight());
    } else {
      Object leftValue = checkValidValue("left", getChildren().get(0).getResult());
      Object rightValue = checkValidValue("right", getChildren().get(1).getResult());

      Object result = performArithmaticOperation(leftValue, rightValue);
      setResult(result);
      end();
    }
  }

  private Object performArithmaticOperation(Object leftValue, Object rightValue) {
    Converter converter = getEngine().getConverter();

    String operation = element.getOperation();

    if (isObject(leftValue) || isArray(leftValue)) {
      leftValue = converter.toPrimitiveDefault(leftValue);
    }

    if (isObject(rightValue) || isArray(rightValue)) {
      rightValue = converter.toPrimitiveDefault(rightValue);
    }

    if ("+".equals(operation)) {
      if (isString(leftValue) || isString(rightValue)) {
        return converter.toString(leftValue) + converter.toString(rightValue);
      }
      if (isUndefined(leftValue) || isUndefined(rightValue)) {
        return Literal.NAN;
      }

      double leftNumber = getDouble(leftValue, converter);
      double rightNumber = getDouble(rightValue, converter);
      return leftNumber + rightNumber;

    } else {

      if (isBoolean(leftValue) || isNull(leftValue)) {
        leftValue = converter.toNumber(leftValue);
      }
      if (isBoolean(rightValue) || isNull(rightValue)) {
        rightValue = converter.toNumber(rightValue);
      }
      if (isNumber(leftValue) && isNumber(rightValue)) {
        double leftNumber = getDouble(leftValue, converter);
        double rightNumber = getDouble(rightValue, converter);

        if ("-".equals(operation)) {
          return leftNumber - rightNumber;

        } else if ("*".equals(operation)) {
          return leftNumber * rightNumber;

        } else if ("/".equals(operation)) {
          if (rightNumber==0d) {
            if (leftNumber==0d) {
              return Literal.NAN;
            } else {
              return Literal.INFINITY;
            }
          }
          return leftNumber / rightNumber;
        }
      }
      return Literal.NAN;
    }
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

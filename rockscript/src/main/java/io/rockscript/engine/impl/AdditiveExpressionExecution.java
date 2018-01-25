package io.rockscript.engine.impl;

import static io.rockscript.engine.impl.Converter.*;
import static io.rockscript.engine.impl.EqualityExpressionExecution.checkValidValue;

public class AdditiveExpressionExecution extends Execution<AdditiveExpression> {

  public AdditiveExpressionExecution(AdditiveExpression additiveExpression, Execution parent) {
    super(parent.createInternalExecutionId(), additiveExpression, parent);
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

      Object result = performArithmaticOperation("+", leftValue, rightValue);
      setResult(result);
      end();
    }
  }

  private Object performArithmaticOperation(String operation, Object leftValue, Object rightValue) {
    Converter converter = getEngine().getConverter();

    if (isObject(leftValue) || isArray(leftValue)) {
      leftValue = converter.toPrimitiveDefault(leftValue);
    }

    if (isObject(rightValue) || isArray(rightValue)) {
      rightValue = converter.toPrimitiveDefault(rightValue);
    }

    if (isString(leftValue) || isString(rightValue)) {
      return converter.toString(leftValue) + converter.toString(rightValue);
    }

    if (isUndefined(leftValue) || isUndefined(rightValue)) {
      return Literal.NAN;
    }

    double leftNumber = getDouble(leftValue, converter);
    double rightNumber = getDouble(rightValue, converter);

    if ("+".equals(operation)) {
      return leftNumber + rightNumber;
    }

    throw new UnsupportedOperationException("TODO");
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

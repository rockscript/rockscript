package io.rockscript.engine.impl;

import io.rockscript.engine.EngineException;

import static io.rockscript.engine.impl.Converter.*;

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
      Object leftValue = getChildren().get(0).getResult();
      Object rightValue = getChildren().get(1).getResult();

      Object result = addObjects(leftValue, rightValue);
      setResult(result);
      end();
    }
  }

  private Object addObjects(Object leftValue, Object rightValue) {
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

    Number leftNumber = converter.toNumber(leftValue);
    Number rightNumber = converter.toNumber(rightValue);
    return  (leftNumber!=null ? leftNumber.doubleValue() : 0) + (rightNumber!=null ? rightNumber.doubleValue() : 0);
  }
}

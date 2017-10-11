package io.rockscript.engine.impl;

import io.rockscript.engine.EngineException;

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

      if (leftValue!=null) {
        if (rightValue!=null) {
          Object result = addObjects(leftValue, rightValue);
          setResult(result);
        } else {
          setResult(leftValue);
        }
      } else if (rightValue!=null) {
        setResult(rightValue);
      }
      end();
    }
  }

  private Object addObjects(Object leftValue, Object rightValue) {
    if (leftValue instanceof String) {
      return (String)leftValue + rightValue.toString();
    }
    throw new EngineException("Only string addition is supported atm", this);
  }
}

package io.rockscript.engine.impl;

import io.rockscript.util.Lists;

import java.util.List;

public class AdditiveExpression extends SingleExpression {

  SingleExpression left;
  SingleExpression right;

  public AdditiveExpression(Integer id, Location location, SingleExpression left, SingleExpression right) {
    super(id, location);
    this.left = left;
    this.right = right;
  }

  @Override
  public Execution createExecution(Execution parent) {
    return new AdditiveExpressionExecution(this, parent);
  }

  @Override
  protected List<? extends ScriptElement> getChildren() {
    return Lists.of(left, right);
  }

  public SingleExpression getLeft() {
    return left;
  }

  public SingleExpression getRight() {
    return right;
  }
}

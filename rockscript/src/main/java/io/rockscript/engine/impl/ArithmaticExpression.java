package io.rockscript.engine.impl;

import io.rockscript.util.Lists;

import java.util.List;

public class ArithmaticExpression extends SingleExpression {

  String operation;
  SingleExpression left;
  SingleExpression right;

  public ArithmaticExpression(Integer id, Location location, String operation, SingleExpression left, SingleExpression right) {
    super(id, location);
    this.operation = operation;
    this.left = left;
    this.right = right;
  }

  @Override
  public Execution createExecution(Execution parent) {
    return new ArithmaticExpressionExecution(this, parent);
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

  public String getOperation() {
    return operation;
  }
}

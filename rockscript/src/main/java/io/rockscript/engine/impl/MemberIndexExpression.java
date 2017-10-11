/*
 * Copyright ©2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rockscript.engine.impl;

import java.util.ArrayList;
import java.util.List;

public class MemberIndexExpression extends SingleExpression {

  SingleExpression baseExpression;
  List<SingleExpression> expressionSequence;

  public MemberIndexExpression(Integer index, Location location) {
    super(index, location);
  }

  public SingleExpression getBaseExpression() {
    return baseExpression;
  }

  public void setBaseExpression(SingleExpression baseExpression) {
    this.baseExpression = baseExpression;
  }

  public List<SingleExpression> getExpressionSequence() {
    return expressionSequence;
  }

  public void setExpressionSequence(List<SingleExpression> expressionSequence) {
    this.expressionSequence = expressionSequence;
  }

  @Override
  public Execution createExecution(Execution parent) {
    return new MemberIndexExpressionExecution(this, parent);
  }

  @Override
  protected List<? extends ScriptElement> getChildren() {
    List<SingleExpression> children = new ArrayList<SingleExpression>();
    children.add(baseExpression);
    children.addAll(expressionSequence);
    return children;
  }
}

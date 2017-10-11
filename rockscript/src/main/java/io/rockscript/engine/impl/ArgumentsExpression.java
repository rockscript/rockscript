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

public class ArgumentsExpression extends SingleExpression {

  SingleExpression functionExpression;
  List<SingleExpression> argumentExpressions;

  public ArgumentsExpression(Integer index, Location location) {
    super(index, location);
  }

  @Override
  public Execution createExecution(Execution parent) {
    return new ArgumentsExpressionExecution(this, parent);
  }

  public SingleExpression getFunctionExpression() {
    return functionExpression;
  }

  public void setFunctionExpression(SingleExpression functionExpression) {
    this.functionExpression = functionExpression;
  }

  public List<SingleExpression> getArgumentExpressions() {
    return argumentExpressions;
  }

  public void setArgumentExpressions(List<SingleExpression> argumentExpressions) {
    this.argumentExpressions = argumentExpressions;
  }

  @Override
  protected List<? extends ScriptElement> getChildren() {
    List<ScriptElement> children = new ArrayList<>();
    if (argumentExpressions!=null) {
      children.addAll(argumentExpressions);
    }
    if (functionExpression!=null) {
      children.add(functionExpression);
    }
    return children;
  }
}

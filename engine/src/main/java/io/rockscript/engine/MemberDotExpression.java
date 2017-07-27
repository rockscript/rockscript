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

package io.rockscript.engine;

import java.util.List;

import io.rockscript.util.Lists;

public class MemberDotExpression extends SingleExpression {

  SingleExpression baseExpression;
  String propertyName;

  public MemberDotExpression(Integer index, Location location) {
    super(index, location);
  }

  public SingleExpression getBaseExpression() {
    return baseExpression;
  }

  public void setBaseExpression(SingleExpression baseExpression) {
    this.baseExpression = baseExpression;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  @Override
  public Execution createExecution(Execution parent) {
    return new MemberDotExpressionExecution(this, parent);
  }

  @Override
  protected List<? extends ScriptElement> getChildren() {
    return Lists.of(baseExpression);
  }
}

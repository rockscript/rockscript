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

import java.util.*;

public class ObjectLiteralExpression extends SingleExpression {

  List<String> propertyNames = new ArrayList<>();
  List<SingleExpression> valueExpressions = new ArrayList<>();

  public ObjectLiteralExpression(String id, Location location) {
    super(id, location);
  }

  @Override
  public Execution createExecution(Execution parent) {
    return new ObjectLiteralExpressionExecution(this, parent);
  }

  @Override
  protected List<Operation> getChildren() {
    return (List) valueExpressions;
  }

  public void addProperty(String propertyName, SingleExpression valueExpression) {
    this.propertyNames.add(propertyName);
    this.valueExpressions.add(valueExpression);
  }

  public List<String> getPropertyNames() {
    return propertyNames;
  }

  public List<SingleExpression> getValueExpressions() {
    return valueExpressions;
  }
}

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

public class ObjectLiteralExpressionExecution extends BlockExecution<ObjectLiteralExpression> {

  public ObjectLiteralExpressionExecution(ObjectLiteralExpression objectLiteralExpression, Execution parent) {
    super(parent.createInternalExecutionId(), objectLiteralExpression, parent);
  }

  @Override
  protected void end() {
    dispatch(new ObjectLiteralExpressionEvent(this));
    applyResult();
    super.end();
  }

  protected void applyResult() {
    Map<String,Object> properties = new LinkedHashMap<>();
    List<String> propertyNames = operation.getPropertyNames();
    for (int i=0; i<children.size(); i++) {
      String propertyName = propertyNames.get(i);
      Object value = children.get(i).getResult();
      properties.put(propertyName, value);
    }
    setResult(properties);
  }
}

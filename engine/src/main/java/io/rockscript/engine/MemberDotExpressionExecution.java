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

import java.util.Map;

public class MemberDotExpressionExecution extends Execution<MemberDotExpression> {

  public MemberDotExpressionExecution(MemberDotExpression operation, Execution parent) {
    super(parent.createInternalExecutionId(), operation, parent);
  }

  @Override
  public void start() {
    startChild(operation.getBaseExpression());
  }

  @Override
  public void childEnded(Execution child) {
    Object propertyValue = getPropertyValue();
    dispatch(new PropertyDereferencedEvent(this, propertyValue));
    setResult(propertyValue);
    end();
  }

  private Object getPropertyValue() {
    Object target = children.get(0).getResult();
    String identifier = getOperation().getPropertyName();
    return getFieldValue(target, identifier);
  }

  public Object getFieldValue(Object target, String identifier) {
    if (target == null) {
      throw new NullPointerException("Cannot evaluate null." + identifier);
    }
    Object fieldValue = null;
    if (target instanceof JsonReadable) {
      JsonReadable jsonObject = (JsonReadable) target;
      fieldValue = jsonObject.get(identifier);
    } else if (target instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String,Object> map = (Map) target;
      fieldValue = map.get(identifier);
    } else {
      throw new RuntimeException("Could not read field value from type " + target.getClass());
    }
    return fieldValue;
  }
}

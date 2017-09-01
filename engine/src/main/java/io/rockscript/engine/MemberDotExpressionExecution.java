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

import io.rockscript.util.Reflection;

import java.lang.reflect.Field;
import java.util.Map;

public class MemberDotExpressionExecution extends Execution<MemberDotExpression> {

  public MemberDotExpressionExecution(MemberDotExpression element, Execution parent) {
    super(parent.createInternalExecutionId(), element, parent);
  }

  @Override
  public void start() {
    startChild(element.getBaseExpression());
  }

  @Override
  public void childEnded(Execution child) {
    Object propertyValue = getPropertyValue();
    // dispatch(new PropertyDereferencedEvent(this, propertyValue));
    setResult(propertyValue);
    end();
  }

  private Object getPropertyValue() {
    Object target = children.get(0).getResult();
    String identifier = getElement().getPropertyName();
    return getFieldValue(target, identifier);
  }

  public Object getFieldValue(Object target, String identifier) {
    Object fieldValue = null;
    if (target instanceof Dereferencable) {
      Dereferencable dereferencable = (Dereferencable) target;
      fieldValue = dereferencable.get(identifier);
    } else if (target instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String,Object> map = (Map) target;
      fieldValue = map.get(identifier);
    } else {
      Field field = Reflection.findFieldInObject(target, identifier);
      if (field!=null) {
        fieldValue = Reflection.getFieldValue(field, target);
      } else {
        throw new RuntimeException("Can't dereference '"+identifier+"': target=" + target);
      }
    }
    return fieldValue;
  }
}

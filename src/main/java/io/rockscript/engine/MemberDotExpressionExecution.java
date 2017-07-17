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
    dispatchAndApply(new MemberDotExpressionEvent(this));
    end();
  }

  public void applyObjectDereference() {
    Object target = children.get(0).getResult();
    String identifier = getOperation().getIdentifier();
    Object fieldValue = getFieldValue(target, identifier);
    setResult(fieldValue);
  }

  public Object getFieldValue(Object target, String identifier) {
    Object fieldValue = null;
    if (target instanceof JsonObject) {
      JsonObject jsonObject = (JsonObject) target;
      fieldValue = jsonObject.get(identifier);
    } else if (target instanceof Map) {
      Map<String,Object> map = (Map) target;
      fieldValue = map.get(identifier);
    } else {
      throw new RuntimeException("TODO: target="+target);
    }
    return fieldValue;
  }
}

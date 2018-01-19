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

import io.rockscript.engine.EngineException;

public class IdentifierExpressionExecution extends Execution<IdentifierExpression> implements Assignable {

  Variable variable = null;

  public IdentifierExpressionExecution(IdentifierExpression element, Execution parent) {
    super(parent.createInternalExecutionId(), element, parent);
  }

  @Override
  public void start() {
    Object identifierValue = getIdentifierValue();
    setResult(identifierValue);
    end();
  }

  public Object getIdentifierValue() {
    String variableName = element.getIdentifier();
    this.variable = parent.getVariable(variableName);
    if (variable!=null) {
      return variable.getValue();
    }
    if ("encodeURI".equals(variableName)) {
      return EncodeUriFunction.INSTANCE;
    }
    throw new EngineException("ReferenceError: "+variableName+" is not defined", this);
  }

  @Override
  public void assign(Object value) {
    if (variable!=null) {
      variable.setValue(value);
    }
  }
}

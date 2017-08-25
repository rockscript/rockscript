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

public class VariableDeclarationExecution extends Execution<VariableDeclaration> {

  public VariableDeclarationExecution(VariableDeclaration element, Execution parent) {
    super(parent.createInternalExecutionId(), element, parent);
  }

  @Override
  public void start() {
    String variableName = element.getVariableName();
    ScriptElement initialValueExpression = element.getInitialiser();
    if (initialValueExpression!=null) {
      startChild(initialValueExpression); // execution will proceed when #childEnded is called
    } else {
      done();
    }
  }

  public void childEnded(Execution child) {
    done();
  }

  public void done() {
    Variable variable = createVariable();

    Object value = variable.getValue();
    Object valueJson = getScript()
        .getConfiguration()
        .getEventStore()
        .valueToJson(value);

    dispatch(new VariableCreatedEvent(this, valueJson));
    parent.childEnded(this);
  }

  private Variable createVariable() {
    VariableDeclaration element = getElement();
    String variableName = element.getVariableName();
    Variable variable = parent.createVariable(variableName);
    Object initialValue = getInitialValue();
    variable.setValue(initialValue);
    return variable;
  }

  private Object getInitialValue() {
    Execution child = children!=null && !children.isEmpty() ? children.get(0) : null;
    return child!=null ? child.getResult() : null;
  }
}

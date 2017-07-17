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

public class VariableCreatedEvent extends Event<VariableDeclarationExecution> {

  public VariableCreatedEvent(VariableDeclarationExecution execution) {
    super(execution);
  }

  @Override
  public EventJson toJson() {
    return new VariableCreatedEventJson(this);
  }

  @Override
  public void apply() {
    VariableDeclaration executable = execution.getOperation();
    String variableName = executable.getVariableName();
    Variable variable = execution.parent.createVariable(variableName);
    variable.setValue(getInitialValue(execution));
  }

  private Object getInitialValue(VariableDeclarationExecution execution) {
    List<Execution> children = execution.getChildren();
    Execution child = children!=null && !children.isEmpty() ? children.get(0) : null;
    return child!=null ? child.getResult() : null;
  }
}

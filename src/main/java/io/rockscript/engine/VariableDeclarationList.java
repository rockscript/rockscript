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

public class VariableDeclarationList extends Statement {

  List<VariableDeclaration> variableDeclarations;

  public VariableDeclarationList(String id, Location location) {
    super(id, location);
  }

  @Override
  public Execution createExecution(Execution parent) {
    return new VariableDeclarationListExecution(this, parent);
  }

  @Override
  protected List<Operation> getChildren() {
    return (List) variableDeclarations;
  }

  public List<VariableDeclaration> getVariableDeclarations() {
    return variableDeclarations;
  }

  public void setVariableDeclarations(List<VariableDeclaration> variableDeclarations) {
    this.variableDeclarations = variableDeclarations;
  }
}

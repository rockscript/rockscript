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

import java.util.List;

public class BlockExecution<T extends ScriptElement> extends Execution<T> {

  public BlockExecution(String id, T element, Execution parent) {
    super(id, element, parent);
  }

  public BlockExecution(T block, Execution parent) {
    super(parent.createInternalExecutionId(), block, parent);
  }

  @Override
  public void start() {
    executeNextStatement();
  }

  public void executeNextStatement() {
    int index = children!=null ? children.size() : 0;
    List<? extends ScriptElement> childElements = element.getChildren();
    if (index < childElements.size()) {
      ScriptElement nextStatement = childElements.get(index);
      startChild(nextStatement);
    } else {
      end();
    }
  }

  @Override
  public void childEnded(Execution child) {
    executeNextStatement();
  }
}

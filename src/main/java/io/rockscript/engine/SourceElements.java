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

/** A list of source elements.  A source element is
 * either a statement or a function declaration. */
public class SourceElements extends Operation {

  List<SourceElement> sourceElements;

  public SourceElements(String id, Location location) {
    super(id, location);
  }

  public List<SourceElement> getSourceElements() {
    return sourceElements;
  }

  public void setSourceElements(List<SourceElement> sourceElements) {
    this.sourceElements = sourceElements;
  }

  public boolean hasSourceElements() {
    return sourceElements!=null;
  }

  public int size() {
    return sourceElements.size();
  }

  public Operation getSourceElement(int position) {
    return sourceElements.get(position);
  }

  @Override
  public Execution createExecution(Execution parent) {
    return new BlockExecution(parent.createInternalExecutionId(), this, parent);
  }

  @Override
  protected List<Operation> getChildren() {
    return (List) sourceElements;
  }
}

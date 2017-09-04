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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Base class for the runtime state of operations. */
public abstract class Execution<T extends ScriptElement> {

  String id;
  T element;
  Execution parent;
  List<Execution> children;
  Map<String, Variable> variables;
  Object result;

  protected Execution(String id, T element, Execution parent) {
    this.id = id;
    this.element = element;
    this.parent = parent;
  }

  protected String createInternalExecutionId() {
    return getScriptExecution().createInternalExecutionId();
  }

  public EngineScriptExecution getScriptExecution() {
    return parent.getScriptExecution();
  }

  public EngineScript getEngineScript() {
    return parent.getEngineScript();
  }

  public abstract void start();

  public void childEnded(Execution child) {
  }

  protected void startChild(ScriptElement childScriptElement) {
    if (childScriptElement==null) {
      EngineScript engineScript = getEngineScript();
      Integer childScriptElementId = childScriptElement.getIndex();
      childScriptElement = engineScript.findScriptElement(childScriptElementId);
      EngineException.throwIfNull(childScriptElement, "Couldn't find engineScript element %s in engineScript %s", childScriptElementId, engineScript
          .getIndex());
    }
    Execution child = createChild(childScriptElement);
    child.start();
  }

  Execution createChild(ScriptElement scriptElement) {
    Execution child = scriptElement.createExecution(this);
    addChild(child);
    return child;
  }

  private void addChild(Execution child) {
    if (children==null) {
      this.children = new ArrayList<>();
    }
    children.add(child);
  }

  protected void end() {
    parent.childEnded(this);
  }

  protected void setResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }

  public Variable createVariable(String variableName) {
    Variable variable = new Variable(variableName);
    if (variables==null) {
      variables = new LinkedHashMap<>();
    }
    variables.put(variableName, variable);
    return variable;
  }

  public Variable getVariable(String variableName) {
    if (hasVariableLocal(variableName)) {
      return variables.get(variableName);
    }
    if (parent!=null) {
      return parent.getVariable(variableName);
    }
    return null;
  }

  public boolean hasVariableLocal(String variableName) {
    return variables!=null && variables.containsKey(variableName);
  }

  /** scans over this and recursively over all children
   * to find the execution with the given id. */
  public Execution findExecutionRecursive(String executionId) {
    if (executionId!=null && executionId.equals(id)) {
      return this;
    }
    if (children!=null && executionId!=null) {
      for (Execution child: children) {
        Execution theOne = child.findExecutionRecursive(executionId);
        if (theOne!=null) {
          return theOne;
        }
      }
    }
    return null;
  }

  protected void dispatch(ExecutionEvent event) {
    getScriptExecution().dispatch(event);
  }

  protected void dispatchAndExecute(ExecutableEvent event) {
    getScriptExecution().dispatchAndExecute(event, this);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public T getElement() {
    return element;
  }

  public Execution getParent() {
    return parent;
  }

  public List<Execution> getChildren() {
    return children;
  }

  public Map<String, Variable> getVariables() {
    return variables;
  }
}
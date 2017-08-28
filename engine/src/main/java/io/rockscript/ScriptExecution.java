/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
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
package io.rockscript;

import io.rockscript.engine.ArgumentsExpressionExecution;
import io.rockscript.engine.EngineScriptExecution;
import io.rockscript.engine.Execution;
import io.rockscript.engine.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptExecution {

  String id;
  Script script;
  Map<String,Object> variableValues;
  List<ActivityContinuation> activityContinuations;
  Boolean ended;

  public ScriptExecution() {
  }

  public ScriptExecution(EngineScriptExecution engineScriptExecution) {
    this.id = engineScriptExecution.getId();
    this.script = engineScriptExecution.getEngineScript().getScript();
    this.ended = engineScriptExecution.isEnded();
    scanVariables(engineScriptExecution.getVariables());
    scan(engineScriptExecution.getChildren());
  }

  private void scan(List<Execution> children) {
    if (children!=null) {
      for (Execution child: children) {
        if (child instanceof ArgumentsExpressionExecution) {
          ActivityContinuation activityContinuation = ((ArgumentsExpressionExecution)child).getActivityContinuation();
          if (activityContinuation!=null) {
            if (activityContinuations==null) {
              activityContinuations = new ArrayList<>();
            }
            activityContinuations.add(activityContinuation);
          }
        }
        scan(child.getChildren());
      }
    }
  }

  private void scanVariables(Map<String, Variable> variables) {
    if (variables!=null) {
      for (Variable variable: variables.values()) {
        if (variableValues==null) {
          variableValues = new HashMap<>();
        }
        variableValues.put(variable.getVariableName(), variable.getValue());
      }
    }
  }

  public Script getScript() {
    return script;
  }

  public List<ActivityContinuation> getActivityContinuations() {
    return activityContinuations;
  }

  public boolean isEnded() {
    return Boolean.TRUE.equals(ended);
  }

  public String getId() {
    return id;
  }

  public Map<String, Object> getVariableValues() {
    return variableValues;
  }

  public Object getVariableValue(String variableName) {
    return variableValues!=null ? variableValues.get(variableName): null;
  }
}

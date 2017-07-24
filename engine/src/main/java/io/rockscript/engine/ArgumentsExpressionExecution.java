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

import java.util.ArrayList;
import java.util.List;

import io.rockscript.action.*;

public class ArgumentsExpressionExecution extends Execution<ArgumentsExpression> {

  public ArgumentsExpressionExecution(ArgumentsExpression operation, Execution parent) {
    super(parent.createInternalExecutionId(), operation, parent);
  }

  @Override
  public void start() {
    startChild(operation.getFunctionExpression());
  }

  @Override
  public void childEnded(Execution child) {
    startNextParameter();
  }

  private void startNextParameter() {
    int parameterIndex = children.size()-1; // -1 because the first one is the function expression
    List<SingleExpression> parameters = operation.getArgumentExpressions();
    if (parameterIndex < parameters.size()) {
      Operation piece = parameters.get(parameterIndex);
      startChild(piece);
    } else {
      Execution functionExpressionExecution = children.get(0);
      Action action = (Action) functionExpressionExecution.getResult();
      if (action instanceof SystemImportAction) {
        invokeSystemImportFunction();
      } else {
        startAction();
      }
    }
  }

  private void invokeSystemImportFunction() {
    // import functions have to be re-executed when the events
    // are applied because they can return functions
    ActionResponse actionResponse = startActionInvoke();
    Object importedObject = actionResponse.getResult();
    dispatch(new ObjectImportedEvent(this, importedObject));
    endActionExecute(importedObject);
  }

  private void startAction() {
    dispatchAndExecute(new ActionStartedEvent(this));
  }

  public void startActionExecute() {
    ActionResponse actionResponse = startActionInvoke();
    if (actionResponse.isEnded()) {
      endAction(actionResponse.getResult());

    } else {
      dispatch(new ActionWaitingEvent(this));
    }
  }

  public void endAction(Object result) {
    dispatchAndExecute(new ActionEndedEvent(this, result));
    // Continues at this.endActionExecute()
  }

  // Continuation from startActionExecute -> ActionEndedEvent
  void endActionExecute(Object result) {
    setResult(result);
    end();
  }

  public ActionResponse startActionInvoke() {
    Execution actionExecution = children.get(0);
    Action action = (Action) actionExecution.getResult();
    List<Object> args = collectArgs();
    // TODO Construct ActionInput and pass that instead
    return action.invoke(new ActionInput(this, args));
  }

  private List<Object> collectArgs() {
    List<Object> args = new ArrayList<>();
    List<Execution> argExecutions = children.subList(1, children.size());
    for (Execution argExecution: argExecutions) {
      args.add(argExecution.getResult());
    }
    return args;
  }

}

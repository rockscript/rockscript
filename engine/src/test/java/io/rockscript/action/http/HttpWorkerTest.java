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
 *
 */
package io.rockscript.action.http;

import java.util.*;

import io.rockscript.Engine;
import io.rockscript.action.ActionResponse;
import io.rockscript.engine.*;
import io.rockscript.test.TestEngine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HttpWorkerTest {

  public static class HttpActionWorker {
    Engine engine;
    List<ActionInput> actionInputQueue = new ArrayList<>();
    public HttpActionWorker(Engine engine) {
      this.engine = engine;
    }
    public void addActionInput(ActionInput actionInput) {
      // Add the action input to the queue
      actionInputQueue.add(actionInput);
      // When the action input is processed async, actionDone should be called
    }
    public void actionDone(ActionOutput actionOutput) {
      engine.endWaitingAction(actionOutput.context, actionOutput.result);
    }
  }

  public static class ActionInput {
    public final ScriptExecutionContext context;
    List<Object> args;
    public ActionInput(String scriptExecutionId, String executionId, List<Object> args) {
      context = new ScriptExecutionContext(scriptExecutionId, executionId);
      this.args = args;
    }
  }

  public static class ActionOutput {
    public final ScriptExecutionContext context;
    Object result;
    public ActionOutput(ActionInput actionInput, Object result) {
      context = actionInput.context;
      this.result = result;
    }
  }


  @Test
  public void testAsyncExecution() {
    TestEngine engine = new TestEngine();
    HttpActionWorker httpActionWorker = new HttpActionWorker(engine);
    ImportResolver importResolver = engine.getServiceLocator().getImportResolver();
    JsonObject http = new JsonObject()
      .put("get", functionInput->{
        // TODO Rename FunctionInput to ActionInput
        ArgumentsExpressionExecution argumentsExpressionExecution = functionInput.getArgumentsExpressionExecution();
        String scriptExecutionId = argumentsExpressionExecution.getScriptExecution().getId();
        String executionId = argumentsExpressionExecution.getId();
        ActionInput actionInput = new ActionInput(scriptExecutionId, executionId, functionInput.getArgs());
        httpActionWorker.addActionInput(actionInput);
        return ActionResponse.waitForFunctionToCompleteAsync();});
    importResolver.add("rockscript.io/http", http);

    String scriptId = engine.deployScript(
      "var http = system.import('rockscript.io/http'); \n" +
      "var interestingData = http.get({ " +
      "  url: 'http://rockscript.io/interesting/data' " +
      "});");

    String scriptExecutionId = engine.startScriptExecution(scriptId);

    ActionInput actionInput = httpActionWorker.actionInputQueue.get(0);

    assertNotNull(actionInput.context.scriptExecutionId);
    assertNotNull(actionInput.context.executionId);

    Map<String,Object> actionInputArgs = (Map<String, Object>) actionInput.args.get(0);
    assertEquals("http://rockscript.io/interesting/data", actionInputArgs.get("url"));

    Map<String,Object> result = new HashMap<>();
    result.put("status", "200");
    ActionOutput actionOutput = new ActionOutput(actionInput, result);
    httpActionWorker.actionDone(actionOutput);

    // TODO check the script
  }

}

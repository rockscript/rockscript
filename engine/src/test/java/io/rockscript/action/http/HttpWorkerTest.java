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
import io.rockscript.action.*;
import io.rockscript.engine.*;
import io.rockscript.TestEngine;
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
  }

  @Test
  public void testAsyncExecution() {
    TestEngine engine = new TestEngine();
    HttpActionWorker httpActionWorker = new HttpActionWorker(engine);
    ImportResolver importResolver = engine.getEngineConfiguration().getImportResolver();
    JsonObject http = new JsonObject()
      .put("get", input -> {
        httpActionWorker.addActionInput(input);
        return ActionOutput.waitForFunctionToCompleteAsync();});
    importResolver.add("rockscript.io/http", http);

    String scriptId = engine
      .deployScript(
        "var http = system.import('rockscript.io/http'); \n" +
        "var interestingData = http.get({ " +
        "  url: 'http://rockscript.io/interesting/data' " +
        "});")
      .getId();

    String scriptExecutionId = engine
      .startScriptExecution(scriptId)
      .getId();

    ActionInput actionInput = httpActionWorker.actionInputQueue.get(0);

    assertNotNull(actionInput.context.scriptExecutionId);
    assertNotNull(actionInput.context.executionId);

    @SuppressWarnings("unchecked")
    Map<String,Object> actionInputArgs = (Map<String, Object>) actionInput.args.get(0);
    assertEquals("http://rockscript.io/interesting/data", actionInputArgs.get("url"));

    Map<String,Object> result = new HashMap<>();
    result.put("status", "200");
    httpActionWorker.engine.endWaitingAction(actionInput.context, result);

    // TODO check the script
  }
}

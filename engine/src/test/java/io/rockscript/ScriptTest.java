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
package io.rockscript;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.rockscript.action.Action;
import io.rockscript.action.ActionOutput;
import io.rockscript.engine.*;
import io.rockscript.test.DeepComparator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ScriptTest {

  protected static Logger log = LoggerFactory.getLogger(ScriptTest.class);

  Engine engine = createTestEngine();
  List<Object> synchronousCapturedData = new ArrayList<>();
  List<String> waitingAsyncFunctionInvocationIds = new ArrayList<>();

  public Engine createTestEngine() {
    TestEngine engine = new TestEngine();
    ImportResolver importResolver = engine.getEngineConfiguration().getImportResolver();
    JsonObject helloService = new JsonObject()
      .put("aSyncFunction", input -> {
          synchronousCapturedData.add("Execution was here");
          synchronousCapturedData.add(input.args.get(0));
          return ActionOutput.endFunction();})
      .put("anAsyncFunction", input -> {
          waitingAsyncFunctionInvocationIds.add(input.context.executionId);
          return ActionOutput.waitForFunctionToCompleteAsync();});
    importResolver.add("example.com/hello", helloService);
    return engine;
  }

  @Test
  public void testAsyncExecution() {
    String scriptId = engine
      .deployScript(
        "var helloService = system.import('example.com/hello'); \n" +
        "var message = 5; \n" +
        "helloService.aSyncFunction(message); \n"+
        "helloService.anAsyncFunction(); \n" +
        "helloService.aSyncFunction('hello');")
      .getId();

    String scriptExecutionId = engine
      .startScriptExecution(scriptId)
      .getId();

    assertEquals("Execution was here", synchronousCapturedData.get(0));
    assertEquals(5d, synchronousCapturedData.get(1));
    assertEquals(2, synchronousCapturedData.size());

    String waitingExecutionId = waitingAsyncFunctionInvocationIds.get(0);
    assertNotNull(waitingExecutionId);

    engine.endWaitingAction(new ScriptExecutionContext(scriptExecutionId, waitingExecutionId));

    assertEquals("Execution was here", synchronousCapturedData.get(2));
    assertEquals("hello", synchronousCapturedData.get(3));
    assertEquals(4, synchronousCapturedData.size());
  }

  @Test
  public void testSerialization() {
    String scriptId = engine
      .deployScript(
        "var helloService = system.import('example.com/hello'); \n" +
        "helloService.anAsyncFunction(); \n" +
        "helloService.aSyncFunction('hello');")
      .getId();

    ScriptExecution scriptExecution = engine
      .startScriptExecution(scriptId);

    String scriptExecutionId = scriptExecution.getId();

    ScriptExecution reloadedScriptExecution = engine
      .getEngineConfiguration()
      .getEventStore()
      .loadScriptExecution(scriptExecutionId);

    DeepComparator deepComparator = new DeepComparator()
      .ignoreField(ScriptExecution.class, "eventListener")
      .ignoreField(Script.class, "elements")
      .ignoreField(Script.class, "engineConfiguration")
      .ignoreField(SystemImportAction.class, "engineConfiguration")
      .ignoreAnonymousField(Action.class, "val$functionHandler")
      .ignoreAnonymousField(Function.class, "arg$1");
    deepComparator.assertEquals(scriptExecution, reloadedScriptExecution);

    String waitingExecutionId = waitingAsyncFunctionInvocationIds.get(0);

    ScriptExecutionContext scriptExecutionContext = new ScriptExecutionContext(scriptExecutionId, waitingExecutionId);
    scriptExecution = engine.endWaitingAction(scriptExecutionContext, null);

    reloadedScriptExecution = engine
      .getEngineConfiguration()
      .getEventStore()
      .loadScriptExecution(scriptExecutionId);

    deepComparator.assertEquals(scriptExecution, reloadedScriptExecution);
  }

}

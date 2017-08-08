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

import io.rockscript.action.ActionOutput;
import io.rockscript.engine.*;
import io.rockscript.test.ScriptExecutionComparator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;
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
          synchronousCapturedData.add(input.getArgs().get(0));
          return ActionOutput.endFunction();})
      .put("anAsyncFunction", input -> {
          waitingAsyncFunctionInvocationIds.add(input.getExecutionId());
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

    log.debug("Starting script...");
    String scriptExecutionId = engine
      .startScriptExecution(scriptId)
      .getId();

    assertEquals(5d, synchronousCapturedData.get(0));
    assertEquals(1, synchronousCapturedData.size());

    log.debug("Ending action...");
    String waitingExecutionId = waitingAsyncFunctionInvocationIds.get(0);
    assertNotNull(waitingExecutionId);
    engine.endWaitingAction(scriptExecutionId, waitingExecutionId);

    assertEquals("hello", synchronousCapturedData.get(1));
    assertEquals(2, synchronousCapturedData.size());
  }

  @Test
  public void testScriptInput() {
    String scriptId = engine
      .deployScript(
        "var helloService = system.import('example.com/hello'); \n" +
        "helloService.aSyncFunction(system.input.greetingOne); \n"+
        "helloService.anAsyncFunction(); \n" +
        "helloService.aSyncFunction(system.input.greetingTwo);")
      .getId();

    String scriptExecutionId = engine
      .startScriptExecution(scriptId, hashMap(
          entry("greetingOne", "hello"),
          entry("greetingTwo", "hi")
      ))
      .getId();

    assertEquals("hello", synchronousCapturedData.get(0));

    String waitingExecutionId = waitingAsyncFunctionInvocationIds.get(0);
    assertNotNull(waitingExecutionId);
    engine.endWaitingAction(scriptExecutionId, waitingExecutionId);

    // This tests that the input is still present after serialization/deserialization
    assertEquals("hi", synchronousCapturedData.get(1));
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
      .findScriptExecutionById(scriptExecutionId);

    new ScriptExecutionComparator()
      .assertEquals(scriptExecution, reloadedScriptExecution);

    String waitingExecutionId = waitingAsyncFunctionInvocationIds.get(0);

    scriptExecution = engine.endWaitingAction(scriptExecutionId, waitingExecutionId);

    reloadedScriptExecution = engine
      .getEngineConfiguration()
      .getEventStore()
      .findScriptExecutionById(scriptExecutionId);

    new ScriptExecutionComparator()
      .assertEquals(scriptExecution, reloadedScriptExecution);
  }
}

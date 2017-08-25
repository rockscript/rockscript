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

import io.rockscript.activity.ActivityOutput;
import io.rockscript.engine.ImportResolver;
import io.rockscript.engine.JsonObject;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.test.ScriptExecutionComparator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ScriptExecutionTest {

  protected static Logger log = LoggerFactory.getLogger(ScriptExecutionTest.class);

  ScriptService scriptService = createTestEngine();
  List<Object> synchronousCapturedData = new ArrayList<>();
  List<String> waitingAsyncFunctionInvocationIds = new ArrayList<>();

  public ScriptService createTestEngine() {
    TestScriptService engine = new TestScriptService();
    ImportResolver importResolver = engine.getEngineConfiguration().getImportResolver();
    JsonObject helloService = new JsonObject()
      .put("aSyncFunction", input -> {
          synchronousCapturedData.add(input.getArgs().get(0));
          return ActivityOutput.endFunction();})
      .put("anAsyncFunction", input -> {
          waitingAsyncFunctionInvocationIds.add(input.getExecutionId());
          return ActivityOutput.waitForFunctionToCompleteAsync();});
    importResolver.add("example.com/hello", helloService);
    return engine;
  }

  @Test
  public void testAsyncExecution() {
    String scriptId = scriptService
      .newDeployScriptCommand()
        .text("var helloService = system.import('example.com/hello'); \n" +
        "var message = 5; \n" +
        "helloService.aSyncFunction(message); \n"+
        "helloService.anAsyncFunction(); \n" +
        "helloService.aSyncFunction('hello');")
        .execute()
      .getId();

    log.debug("Starting script...");
    String scriptExecutionId = scriptService
      .startScriptExecution(scriptId)
      .getId();

    assertEquals(5d, synchronousCapturedData.get(0));
    assertEquals(1, synchronousCapturedData.size());

    log.debug("Ending activity...");
    String waitingExecutionId = waitingAsyncFunctionInvocationIds.get(0);
    assertNotNull(waitingExecutionId);
    scriptService.endActivity(scriptExecutionId, waitingExecutionId);

    assertEquals("hello", synchronousCapturedData.get(1));
    assertEquals(2, synchronousCapturedData.size());
  }

  @Test
  public void testScriptInput() {
    String scriptId = scriptService
      .newDeployScriptCommand()
        .text(
        "var helloService = system.import('example.com/hello'); \n" +
        "helloService.aSyncFunction(system.input.greetingOne); \n"+
        "helloService.anAsyncFunction(); \n" +
        "helloService.aSyncFunction(system.input.greetingTwo);")
        .execute()
      .getId();

    String scriptExecutionId = scriptService
      .startScriptExecution(scriptId, hashMap(
          entry("greetingOne", "hello"),
          entry("greetingTwo", "hi")
      ))
      .getId();

    assertEquals("hello", synchronousCapturedData.get(0));

    String waitingExecutionId = waitingAsyncFunctionInvocationIds.get(0);
    assertNotNull(waitingExecutionId);
    scriptService.endActivity(scriptExecutionId, waitingExecutionId);

    // This tests that the input is still present after serialization/deserialization
    assertEquals("hi", synchronousCapturedData.get(1));
  }

  @Test
  public void testSerialization() {
    String scriptId = scriptService
      .newDeployScriptCommand()
        .text(
        "var helloService = system.import('example.com/hello'); \n" +
        "helloService.anAsyncFunction(); \n" +
        "helloService.aSyncFunction('hello');")
        .execute()
      .getId();

    ScriptExecution scriptExecution = scriptService
      .startScriptExecution(scriptId);

    String scriptExecutionId = scriptExecution.getId();

    ScriptExecution reloadedScriptExecution = scriptService
      .getEngineConfiguration()
      .getEventStore()
      .findScriptExecutionById(scriptExecutionId);

    new ScriptExecutionComparator()
      .assertEquals(scriptExecution, reloadedScriptExecution);

    String waitingExecutionId = waitingAsyncFunctionInvocationIds.get(0);

    scriptExecution = scriptService.endActivity(scriptExecutionId, waitingExecutionId);

    reloadedScriptExecution = scriptService
      .getEngineConfiguration()
      .getEventStore()
      .findScriptExecutionById(scriptExecutionId);

    new ScriptExecutionComparator()
      .assertEquals(scriptExecution, reloadedScriptExecution);
  }
}

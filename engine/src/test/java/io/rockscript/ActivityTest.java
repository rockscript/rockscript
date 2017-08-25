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

import io.rockscript.activity.ActivityInput;
import io.rockscript.activity.ActivityOutput;
import io.rockscript.engine.JsonObject;
import io.rockscript.engine.Script;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.test.ScriptTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ActivityTest extends ScriptTest {

  protected static Logger log = LoggerFactory.getLogger(ActivityTest.class);

  List<ActivityInput> continuationReferences = new ArrayList<>();

  @Override
  protected ScriptService initializeScriptService() {
    // This ensures that each test will get a new ScriptService
    // so that the tests can customize the import resolver without
    // polluting any cached script services.
    return new TestConfiguration().build();
  }

  @Test
  public void testAsynchronousActivity() {
    scriptService.getConfiguration().getImportResolver().add(
      "approvalService", new JsonObject()
        .put("approve", input -> {
          continuationReferences.add(input);
          return ActivityOutput.waitForFunctionToCompleteAsync();
        }));

    Script script = deployScript(
      "var approvalService = system.import('approvalService'); \n" +
      "approvalService.approve('primus'); ");

    ScriptExecution scriptExecution = startScriptExecution(script);

    ActivityInput continuationReference = continuationReferences.get(0);
    assertEquals("primus", continuationReference.getArgs().get(0));
    assertEquals(1, continuationReferences.size());
    assertFalse(scriptExecution.isEnded());

    String scriptExecutionId = scriptExecution.getId();
    String executionId = continuationReference.getExecutionId();
    assertNotNull(executionId);

    scriptExecution = endActivity(scriptExecutionId, executionId);
    assertTrue(scriptExecution.isEnded());
  }

  @Test
  public void testSynchronousActivityWithoutResult() {
    scriptService.getConfiguration().getImportResolver().add(
      "approvalService", new JsonObject()
        .put("approve", input -> {
          return ActivityOutput.endFunction();
        }));

    Script script = deployScript(
      "var approvalService = system.import('approvalService'); \n" +
      "approvalService.approve('primus'); ");

    ScriptExecution scriptExecution = startScriptExecution(script);

    assertEquals(0, continuationReferences.size());
    assertTrue(scriptExecution.isEnded());
  }

  @Test
  public void testSynchronousActivityWithResult() {
    scriptService.getConfiguration().getImportResolver().add(
        "approvalService", new JsonObject()
            .put("approve", input -> {
              return ActivityOutput.endFunction("approved");
            }));

    Script script = deployScript(
        "var approvalService = system.import('approvalService'); \n" +
            "var approveResult = approvalService.approve('primus'); ");

    ScriptExecution scriptExecution = startScriptExecution(script);

    Object approveResult = scriptExecution.getVariable("approveResult").getValue();
    assertEquals("approved", approveResult);
    assertTrue(scriptExecution.isEnded());
  }

//
//  @Test
//  public void testSerialization() {
//    String scriptId = scriptService
//      .newDeployScriptCommand()
//        .text(
//        "var helloService = system.import('example.com/hello'); \n" +
//        "helloService.anAsyncFunction(); \n" +
//        "helloService.aSyncFunction('hello');")
//        .execute()
//      .getId();
//
//    ScriptExecution scriptExecution = scriptService
//      .startScriptExecution(scriptId);
//
//    String scriptExecutionId = scriptExecution.getId();
//
//    ScriptExecution reloadedScriptExecution = scriptService
//      .getConfiguration()
//      .getEventStore()
//      .findScriptExecutionById(scriptExecutionId);
//
//    new ScriptExecutionComparator()
//      .assertEquals(scriptExecution, reloadedScriptExecution);
//
//    String waitingExecutionId = waitingAsyncFunctionInvocationIds.get(0);
//
//    scriptExecution = scriptService.endActivity(scriptExecutionId, waitingExecutionId);
//
//    reloadedScriptExecution = scriptService
//      .getConfiguration()
//      .getEventStore()
//      .findScriptExecutionById(scriptExecutionId);
//
//    new ScriptExecutionComparator()
//      .assertEquals(scriptExecution, reloadedScriptExecution);
//  }
}

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
import io.rockscript.request.RequestExecutorService;
import io.rockscript.engine.Script;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.engine.TestConfiguration;
import io.rockscript.engine.impl.ExecutionEvent;
import io.rockscript.test.ScriptTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ActivityTest extends ScriptTest {

  protected static Logger log = LoggerFactory.getLogger(ActivityTest.class);

  List<ActivityInput> activityInputs = new ArrayList<>();

  @Override
  protected RequestExecutorService initializeScriptService() {
    // This ensures that each test will get a new RequestExecutorService
    // so that the tests can customize the import resolver without
    // polluting any cached script services.
    return new TestConfiguration().build();
  }

  @Test
  public void testAsynchronousActivity() {
    getConfiguration().getImportResolver().createImport("approvalService")
      .put("approve", input -> {
        activityInputs.add(input);
        return ActivityOutput.waitForEndActivityCallback();
      });

    Script script = deployScript(
      "var approvalService = system.import('approvalService'); \n" +
      "approvalService.approve('primus'); ");

    ScriptExecution scriptExecution = startScriptExecution(script);

    ActivityInput activityInput = activityInputs.get(0);
    assertEquals("primus", activityInput.getArgs().get(0));
    assertEquals(1, activityInputs.size());
    assertFalse(scriptExecution.isEnded());

    String scriptExecutionId = scriptExecution.getId();
    String executionId = activityInput.getExecutionId();
    assertNotNull(executionId);

    scriptExecution = endActivity(activityInput.getContinuationReference());
    assertTrue(scriptExecution.isEnded());
  }

  @Test
  public void testEvents() {
    getConfiguration().getImportResolver().createImport("approvalService")
      .put("getMessage", input -> {
        input.getEngine().endActivity(input.getContinuationReference(), "hello");
        return ActivityOutput.waitForEndActivityCallback();
      }).put("approve", input -> {
      activityInputs.add(input);
      return ActivityOutput.waitForEndActivityCallback();
      });

    Script script = deployScript(
      "var approvalService = system.import('approvalService'); \n" +
      "var msg = approvalService.getMessage(); " +
      "approvalService.approve(msg); ");

    ScriptExecution scriptExecution = startScriptExecution(script);

    ActivityInput activityInput = activityInputs.get(0);
    scriptExecution = endActivity(activityInput.getContinuationReference());

    List<ExecutionEvent> events = getConfiguration().getEventStore()
      .findEventsByScriptExecutionId(scriptExecution.getId());
    events.forEach(e->log.debug(e.toString()));
  }

  @Test
  public void testSynchronousActivityWithoutResult() {
    getConfiguration().getImportResolver().createImport("approvalService")
      .put("approve", input -> {
        return ActivityOutput.endActivity();
      });

    Script script = deployScript(
      "var approvalService = system.import('approvalService'); \n" +
      "approvalService.approve('primus'); ");

    ScriptExecution scriptExecution = startScriptExecution(script);

    assertEquals(0, activityInputs.size());
    assertTrue(scriptExecution.isEnded());
  }

  @Test
  public void testSynchronousActivityWithResult() {
    getConfiguration().getImportResolver().createImport("approvalService")
      .put("approve", input -> {
        return ActivityOutput.endActivity("approved");
      });

    Script script = deployScript(
        "var approvalService = system.import('approvalService'); \n" +
            "var approveResult = approvalService.approve('primus'); ");

    ScriptExecution scriptExecution = startScriptExecution(script);

    Object approveResult = scriptExecution.getVariable("approveResult");
    assertEquals("approved", approveResult);
    assertTrue(scriptExecution.isEnded());
  }
}

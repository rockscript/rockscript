/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.rockscript.test.engine;

import io.rockscript.Engine;
import io.rockscript.TestEngine;
import io.rockscript.activity.ActivityInput;
import io.rockscript.activity.ActivityOutput;
import io.rockscript.api.model.ScriptExecution;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.ExecutionEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ActivityTest extends AbstractEngineTest {

  protected static Logger log = LoggerFactory.getLogger(ActivityTest.class);

  List<ActivityInput> activityInputs = new ArrayList<>();

  @Override
  protected Engine initializeEngine() {
    // This ensures that each test will get a new CommandExecutorService
    // so that the tests can customize the import resolver without
    // polluting any cached script services.
    return new TestEngine().initialize();
  }

  @Test
  public void testAsynchronousActivity() {
    engine.getImportResolver().createImport("approvalService")
      .put("approve", input -> {
        activityInputs.add(input);
        return ActivityOutput.waitForEndActivityCallback();
      });

    ScriptVersion scriptVersion = deployScript(
      "var approvalService = system.import('approvalService'); \n" +
      "approvalService.approve('primus'); ");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion);

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
    engine.getImportResolver().createImport("approvalService")
      .put("getMessage", input -> {
        input.getScriptRunner().endActivity(input.getContinuationReference(), "hello");
        return ActivityOutput.waitForEndActivityCallback();
      }).put("approve", input -> {
      activityInputs.add(input);
      return ActivityOutput.waitForEndActivityCallback();
      });

    ScriptVersion scriptVersion = deployScript(
      "var approvalService = system.import('approvalService'); \n" +
      "var msg = approvalService.getMessage(); " +
      "approvalService.approve(msg); ");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion);

    ActivityInput activityInput = activityInputs.get(0);
    scriptExecution = endActivity(activityInput.getContinuationReference());

    List<ExecutionEvent> events = engine.getEventStore()
      .findEventsByScriptExecutionId(scriptExecution.getId());
    events.forEach(e->log.debug(e.toString()));
  }

  @Test
  public void testSynchronousActivityWithoutResult() {
    engine.getImportResolver().createImport("approvalService")
      .put("approve", input -> {
        return ActivityOutput.endActivity();
      });

    ScriptVersion scriptVersion = deployScript(
      "var approvalService = system.import('approvalService'); \n" +
      "approvalService.approve('primus'); ");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion);

    assertEquals(0, activityInputs.size());
    assertTrue(scriptExecution.isEnded());
  }

  @Test
  public void testSynchronousActivityWithResult() {
    engine.getImportResolver().createImport("approvalService")
      .put("approve", input -> {
        return ActivityOutput.endActivity("approved");
      });

    ScriptVersion scriptVersion = deployScript(
        "var approvalService = system.import('approvalService'); \n" +
            "var approveResult = approvalService.approve('primus'); ");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion);

    Object approveResult = scriptExecution.getVariable("approveResult");
    assertEquals("approved", approveResult);
    assertTrue(scriptExecution.isEnded());
  }
}

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

import io.rockscript.test.TestEngine;
import io.rockscript.api.model.ScriptExecution;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.api.events.ExecutionEvent;
import io.rockscript.engine.impl.LockOperationEnd;
import io.rockscript.service.ServiceFunctionInput;
import io.rockscript.service.ServiceFunctionOutput;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ServiceFunctionTest extends AbstractEngineTest {

  protected static Logger log = LoggerFactory.getLogger(ServiceFunctionTest.class);

  List<ServiceFunctionInput> inputs = new ArrayList<>();

  @Override
  protected TestEngine initializeEngine() {
    // This ensures that each test will get a new CommandExecutorService
    // so that the tests can customize the import resolver without
    // polluting any cached script services.
    return new TestEngine().start();
  }

  @Test
  public void testAsynchronousServiceFunction() {
    engine.getImportResolver().createImport("approvalService")
      .put("approve", input -> {
        inputs.add(input);
        return ServiceFunctionOutput.waitForFunctionEndCallback();
      });

    ScriptVersion scriptVersion = deployScript(
      "var approvalService = system.import('approvalService'); \n" +
      "approvalService.approve('primus'); ");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion);

    ServiceFunctionInput input = inputs.get(0);
    assertEquals("primus", input.getArgs().get(0));
    assertEquals(1, inputs.size());
    assertFalse(scriptExecution.isEnded());

    String scriptExecutionId = scriptExecution.getId();
    String executionId = input.getExecutionId();
    assertNotNull(executionId);

    scriptExecution = endFunction(input.getContinuationReference());
    assertTrue(scriptExecution.isEnded());
  }

  @Test
  public void testEvents() {
    engine.getImportResolver().createImport("approvalService")
      .put("getMessage", input -> {
        input.getScriptRunner().executeInLock(new LockOperationEnd(input.getContinuationReference(), "hello"));
        return ServiceFunctionOutput.waitForFunctionEndCallback();
      }).put("approve", input -> {
      inputs.add(input);
      return ServiceFunctionOutput.waitForFunctionEndCallback();
      });

    ScriptVersion scriptVersion = deployScript(
      "var approvalService = system.import('approvalService'); \n" +
      "var msg = approvalService.getMessage(); " +
      "approvalService.approve(msg); ");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion);

    ServiceFunctionInput input = inputs.get(0);
    scriptExecution = endFunction(input.getContinuationReference());

    List<ExecutionEvent> events = engine.getEventStore()
      .findEventsByScriptExecutionId(scriptExecution.getId());
    events.forEach(e->log.debug(e.toString()));
  }

  @Test
  public void testSynchronousServiceFunctionWithoutResult() {
    engine.getImportResolver().createImport("approvalService")
      .put("approve", input -> {
        return ServiceFunctionOutput.endFunction();
      });

    ScriptVersion scriptVersion = deployScript(
      "var approvalService = system.import('approvalService'); \n" +
      "approvalService.approve('primus'); ");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion);

    assertEquals(0, inputs.size());
    assertTrue(scriptExecution.isEnded());
  }

  @Test
  public void testSynchronousServiceFunctionWithResult() {
    engine.getImportResolver().createImport("approvalService")
      .put("approve", input -> {
        return ServiceFunctionOutput.endFunction("approved");
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

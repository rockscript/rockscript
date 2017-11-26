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

import io.rockscript.activity.test.TestError;
import io.rockscript.activity.test.TestResult;
import io.rockscript.activity.test.TestResults;
import io.rockscript.api.commands.RunTestsCommand;
import io.rockscript.api.commands.SaveScriptVersionCommand;
import io.rockscript.engine.impl.ActivityStartErrorEvent;
import io.rockscript.engine.impl.Event;
import io.rockscript.engine.impl.ScriptExecutionErrorEvent;
import io.rockscript.http.servlet.PathRequestHandler;
import io.rockscript.http.servlet.RouterServlet;
import io.rockscript.http.servlet.ServerRequest;
import io.rockscript.http.servlet.ServerResponse;
import io.rockscript.test.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.rockscript.http.servlet.PathRequestHandler.GET;
import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;
import static org.junit.Assert.*;

public class TestRunnerHttpTest extends AbstractHttpTest {

  protected static Logger log = LoggerFactory.getLogger(TestRunnerHttpTest.class);

  @Override
  protected void configure(RouterServlet routerServlet) {
    routerServlet
      .requestHandler(new PathRequestHandler(GET, "/ole") {
        @SuppressWarnings("unchecked")
        @Override
        public void handle(ServerRequest request, ServerResponse response) {
          response.status(200)
            .bodyJson(hashMap(
              entry("country", "Belgium"),
              entry("currency", "EUR")));
        }
      });
  }

  @Test
  public void testTestRunnerAssertionFailure() {
    new SaveScriptVersionCommand()
        .scriptName("The ScriptVersion.rs")
        .scriptText(
          "var http = system.import('rockscript.io/http'); \n" +
          "var country = http \n" +
          "  .get({url: 'http://localhost:4000/ole'}) \n" +
          "  .body.country;")
        .activate()
        .execute(engine);

    String testScriptId = new SaveScriptVersionCommand()
        .scriptName("The ScriptVersion Test.rst")
        .scriptText(
          "var test = system.import('rockscript.io/test'); \n" +
          "var scriptExecution = test.start({ \n" +
          "  script: 'The ScriptVersion.rs', \n" +
          "  skipActivities: true}); \n" +
          "test.assertEquals(scriptExecution.variables.country, 'The Netherlands');")
        .activate()
        .execute(engine)
        .getId();

    TestResults testResults = new RunTestsCommand()
      .execute(engine);

    log.debug(engine.getGson().toJson(testResults));

    TestResult testResult = testResults.get(0);

    log.debug("Events:");
    testResult.getEvents().forEach(e->log.debug(e.toString()));
    log.debug("Errors:");
    testResult.getErrors().forEach(e->log.debug(e.toString()));

    List<Event> testEvents = testResult.getEvents();
    ActivityStartErrorEvent errorEvent = (ActivityStartErrorEvent) testEvents.get(testEvents.size() - 1);

    Assert.assertContains("Expected The Netherlands, but was Belgium", errorEvent.getError());
    assertNull(errorEvent.getRetryTime()); // because there's no point in retrying assertion errors

    TestError testError = testResult.getErrors().get(0);
    Assert.assertContains("Expected The Netherlands, but was Belgium", testError.getMessage());
    assertEquals(testScriptId, testError.getScriptVersionId());
    assertEquals(5, testError.getLine());
  }

  @Test
  public void testTestRunnerScriptFailure() {
    String targetScriptId = new SaveScriptVersionCommand()
        .scriptName("The ScriptVersion.rs")
        .scriptText(
          /* 1 */ "var http = system.import('rockscript.io/http'); \n" +
          /* 2 */ "unexistingvar.unexistingmethod();")
        .activate()
        .execute(engine)
        .getId();

    String testScriptId = new SaveScriptVersionCommand()
        .scriptName("The ScriptVersion Test.rst")
        .scriptText(
          /* 1 */ "var test = system.import('rockscript.io/test'); \n" +
          /* 2 */ "\n" +
          /* 3 */ "var scriptExecution = test.start({ \n" +
          /* 4 */ "  script: 'The ScriptVersion.rs', \n" +
          /* 5 */ "  skipActivities: true}); ")
        .activate()
        .execute(engine)
        .getId();

    TestResults testResults = new RunTestsCommand()
      .execute(engine);

    log.debug(engine.getGson().toJson(testResults));

    TestResult testResult = testResults.get(0);

    log.debug("Events:");
    testResult.getEvents().forEach(e->log.debug(e.toString()));
    log.debug("Errors:");
    testResult.getErrors().forEach(e->log.debug(e.toString()));

    List<Event> testEvents = testResult.getEvents();
    ScriptExecutionErrorEvent targetScriptErrorEvent = (ScriptExecutionErrorEvent) testEvents.get(testEvents.size() - 2);
    Assert.assertContains("ReferenceError: unexistingvar is not defined", targetScriptErrorEvent.getError());
    Assert.assertContains(targetScriptId, targetScriptErrorEvent.getScriptId());
    assertNotNull(targetScriptErrorEvent.getLine());

    ActivityStartErrorEvent testScriptErrorEvent = (ActivityStartErrorEvent) testEvents.get(testEvents.size() - 1);
    Assert.assertContains("ScriptVersion start failed: ReferenceError: unexistingvar is not defined", testScriptErrorEvent.getError());
    Assert.assertContains(testScriptId, testScriptErrorEvent.getScriptId());
    assertNotNull(testScriptErrorEvent.getLine());

    List<TestError> testErrors = testResult.getErrors();
    TestError firstTestError = testErrors.get(0);
    Assert.assertContains("ReferenceError: unexistingvar is not defined", firstTestError.getMessage());
    Assert.assertContains(targetScriptId, firstTestError.getScriptVersionId());
    assertNotNull(firstTestError.getLine());

    TestError secondTestError = testErrors.get(1);
    Assert.assertContains("ScriptVersion start failed: ReferenceError: unexistingvar is not defined", secondTestError.getMessage());
    Assert.assertContains(testScriptId, secondTestError.getScriptVersionId());
    assertNotNull(secondTestError.getLine());
  }
}

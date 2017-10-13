/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
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
 */
package io.rockscript;

import io.rockscript.activity.test.TestResult;
import io.rockscript.activity.test.TestResults;
import io.rockscript.activity.test.TestError;
import io.rockscript.engine.impl.ActivityStartErrorEvent;
import io.rockscript.engine.impl.Event;
import io.rockscript.engine.impl.ScriptExecutionErrorEvent;
import io.rockscript.test.HttpTest;
import io.rockscript.test.HttpTestServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestRunnerTest extends HttpTest {

  protected static Logger log = LoggerFactory.getLogger(TestRunnerTest.class);

  @Override
  protected void configure(HttpTestServer httpTestServer) {
    httpTestServer.get("/ole", (request, response) -> {
      response.status(200)
        .headerContentTypeApplicationJson()
        .body(gson.toJson(hashMap(
          entry("country", "Belgium"),
          entry("currency", "EUR"))))
        .send();
    });
  }

  @Test
  public void testTestRunnerAssertionFailure() {
    scriptService.newDeployScriptCommand()
      .scriptText(
        "var http = system.import('rockscript.io/http'); \n" +
        "var country = http \n" +
        "  .get({url: 'http://localhost:4000/ole'}) \n" +
        "  .body.country;")
      .scriptName("The Script.rs")
      .execute();

    String testScriptId = scriptService.newDeployScriptCommand()
      .scriptText(
        "var test = system.import('rockscript.io/test'); \n" +
        "var scriptExecution = test.start({ \n" +
        "  script: 'The Script.rs', \n" +
        "  skipActivities: true}); \n" +
        "test.assertEquals(scriptExecution.variables.country, 'The Netherlands');")
      .scriptName("The Script Test.rst")
      .execute()
      .getId();

    TestResults testResults = scriptService
      .newRunTestsCommand()
      .execute();

    log.debug(getConfiguration().getGson().toJson(testResults));

    TestResult testResult = testResults.get(0);

    log.debug("Events:");
    testResult.getEvents().forEach(e->log.debug(e.toString()));
    log.debug("Errors:");
    testResult.getErrors().forEach(e->log.debug(e.toString()));

    List<Event> testEvents = testResult.getEvents();
    ActivityStartErrorEvent errorEvent = (ActivityStartErrorEvent) testEvents.get(testEvents.size() - 1);

    assertContains("Expected The Netherlands, but was Belgium", errorEvent.getError());
    assertNull(errorEvent.getRetryTime()); // because there's no point in retrying assertion errors

    TestError testError = testResult.getErrors().get(0);
    assertContains("Expected The Netherlands, but was Belgium", testError.getMessage());
    assertEquals(testScriptId, testError.getScriptId());
    assertEquals(5, testError.getLine());
  }

  @Test
  public void testTestRunnerScriptFailure() {
    String targetScriptId = scriptService.newDeployScriptCommand()
      .scriptText(
        /* 1 */ "var http = system.import('rockscript.io/http'); \n" +
        /* 2 */ "unexistingvar.unexistingmethod();")
      .scriptName("The Script.rs")
      .execute()
      .getId();

    String testScriptId = scriptService.newDeployScriptCommand()
      .scriptText(
        /* 1 */ "var test = system.import('rockscript.io/test'); \n" +
        /* 2 */ "\n" +
        /* 3 */ "var scriptExecution = test.start({ \n" +
        /* 4 */ "  script: 'The Script.rs', \n" +
        /* 5 */ "  skipActivities: true}); ")
      .scriptName("The Script Test.rst")
      .execute()
      .getId();

    TestResults testResults = scriptService
      .newRunTestsCommand()
      .execute();

    log.debug(getConfiguration().getGson().toJson(testResults));

    TestResult testResult = testResults.get(0);

    log.debug("Events:");
    testResult.getEvents().forEach(e->log.debug(e.toString()));
    log.debug("Errors:");
    testResult.getErrors().forEach(e->log.debug(e.toString()));

    List<Event> testEvents = testResult.getEvents();
    ScriptExecutionErrorEvent targetScriptErrorEvent = (ScriptExecutionErrorEvent) testEvents.get(testEvents.size() - 2);
    assertContains("ReferenceError: unexistingvar is not defined", targetScriptErrorEvent.getError());
    assertContains(targetScriptId, targetScriptErrorEvent.getScriptId());
    assertNotNull(targetScriptErrorEvent.getLine());

    ActivityStartErrorEvent testScriptErrorEvent = (ActivityStartErrorEvent) testEvents.get(testEvents.size() - 1);
    assertContains("Script start failed: ReferenceError: unexistingvar is not defined", testScriptErrorEvent.getError());
    assertContains(testScriptId, testScriptErrorEvent.getScriptId());
    assertNotNull(testScriptErrorEvent.getLine());

    List<TestError> testErrors = testResult.getErrors();
    TestError firstTestError = testErrors.get(0);
    assertContains("ReferenceError: unexistingvar is not defined", firstTestError.getMessage());
    assertContains(targetScriptId, firstTestError.getScriptId());
    assertNotNull(firstTestError.getLine());

    TestError secondTestError = testErrors.get(1);
    assertContains("Script start failed: ReferenceError: unexistingvar is not defined", secondTestError.getMessage());
    assertContains(testScriptId, secondTestError.getScriptId());
    assertNotNull(secondTestError.getLine());
  }
}

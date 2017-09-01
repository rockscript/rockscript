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

import io.rockscript.activity.test.TestResults;
import io.rockscript.engine.ErrorMessage;
import io.rockscript.test.HttpTest;
import io.rockscript.test.HttpTestServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.rockscript.util.Exceptions.assertContains;
import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;
import static org.junit.Assert.assertEquals;

public class TestRunnerTest extends HttpTest {

  protected static Logger log = LoggerFactory.getLogger(TestRunnerTest.class);

  @Override
  protected void configure(HttpTestServer httpTestServer) {
    httpTestServer
      .get("/ole", (request,response)-> {
        response
          .status(200)
          .headerContentTypeApplicationJson()
          .body(gson.toJson(hashMap(
              entry("country", "Belgium"),
              entry("currency", "EUR")
            )
          ))
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
                "  scriptName: 'The Script.rs', \n" +
                "  skipActivities: true}); \n" +
                "test.assertEquals(scriptExecution.variables.country, 'The Netherlands');")
        .scriptName("The Script Test.rst")
        .execute()
        .getId();

    TestResults testResults = scriptService.newRunTestsCommand()
        .tests("*.rst")
        .execute();

    log.debug(getConfiguration().getGson().toJson(testResults));

    ErrorMessage error = testResults.get(0).getError();
    assertContains("Expected 'The Netherlands'", error.getMessage());
    assertContains("but was 'Belgium'", error.getMessage());

    assertEquals(testScriptId, error.getScriptId());
    assertEquals(5, error.getLocation().getLine());
  }

  @Test
  public void testTestRunnerScriptFailure() {
    String scriptId = scriptService.newDeployScriptCommand()
        .scriptText(
            "var http = system.import('rockscript.io/http'); \n" +
                "unexistingvar.unexistingmethod();")
        .scriptName("The Script.rs")
        .execute()
        .getId();

    scriptService.newDeployScriptCommand()
        .scriptText(
            "var test = system.import('rockscript.io/test'); \n" +
                "var scriptExecution = test.start({ \n" +
                "  scriptName: 'The Script.rs', \n" +
                "  skipActivities: true}); ")
        .scriptName("The Script Test.rst")
        .execute();

    TestResults testResults = scriptService.newRunTestsCommand()
        .tests("*.rst")
        .execute();

    log.debug(getConfiguration().getGson().toJson(testResults));

    ErrorMessage error = testResults.get(0).getError();
    assertEquals("ReferenceError: unexistingvar is not defined", error.getMessage());

    assertEquals(scriptId, error.getScriptId());
    assertEquals(2, error.getLocation().getLine());
  }
}

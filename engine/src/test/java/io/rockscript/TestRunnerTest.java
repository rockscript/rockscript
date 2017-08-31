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

import io.rockscript.activity.ActivityOutput;
import io.rockscript.test.HttpTest;
import io.rockscript.test.HttpTestServer;
import io.rockscript.activity.test.TestResults;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;

public class TestRunnerTest extends HttpTest {

  protected static Logger log = LoggerFactory.getLogger(TestRunnerTest.class);

  @Override
  protected void configure(HttpTestServer httpTestServer) {
    httpTestServer
      .get("/ole", (request,response)-> {
        response
          .status(200)
          .headerContentTypeApplicationJson()
          .body(gson.toJson(ActivityOutput.endFunction(
            hashMap(
              entry("country", "Belgium"),
              entry("currency", "EUR")
            )
          )))
          .send();
      });
  }

  @Test
  public void testTestRunner() {
    scriptService.newDeployScriptCommand()
        .scriptText(
            "var http = system.import('rockscript.io/http'); \n" +
                "var ole = http.get({url: 'http://localhost:4000/ole'}); ")
        .scriptName("the-get-script.rs")
        .execute();

    scriptService.newDeployScriptCommand()
        .scriptText(
            "var test = system.import('rockscript.io/test'); \n" +
            "var se = test.start({" +
              "scriptName: 'the-get-script.rs'," +
              "skipActivities: true}); "
               // +"test.assertEquals(se.variables.ole.body.country, 'Belgium');"
        )
        .scriptName("*.rst")
        .execute();

    TestResults scriptTestResult = scriptService.newRunTestsCommand()
        .tests("*.rst")
        .execute();

    log.debug(getConfiguration().getGson().toJson(scriptTestResult));
  }
}

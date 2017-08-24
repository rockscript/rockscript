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

import com.google.gson.Gson;
import io.rockscript.activity.ActivityInput;
import io.rockscript.activity.ActivityOutput;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.test.HttpTest;
import io.rockscript.test.HttpTestServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;
import static org.junit.Assert.assertEquals;

public class HttpSynchronousActivityTest extends HttpTest {

  protected static Logger log = LoggerFactory.getLogger(HttpSynchronousActivityTest.class);

  ScriptService scriptService = createTestEngine();
  Gson gson = scriptService.getEngineConfiguration().getGson();
  List<ActivityInput> activityInputs = new ArrayList<>();

  public ScriptService createTestEngine() {
    return new TestScriptService();
  }

  @Override
  protected void configure(HttpTestServer httpTestServer) {
    httpTestServer
      .post("/approve", (request,response)-> {
        ActivityInput activityInput = gson.fromJson(request.body(), ActivityInput.class);
        activityInputs.add(activityInput);
        ActivityOutput activityOutput = ActivityOutput.endFunction(
          hashMap(
            entry("country", "Belgium"),
            entry("currency", "EUR")
          )
        );
        response
            .status(200)
            .headerContentTypeApplicationJson()
            .body(gson.toJson(activityOutput));
      });
  }

  @Test
  public void testHttpActivity() {
    String scriptId = scriptService
      .deployScript(
        "var approvals = system.import('localhost:"+PORT+"'); \n" +
        "var currency = approvals.approve('oo',7).currency; ")
      .getId();

    ScriptExecution scriptExecution = scriptService
      .startScriptExecution(scriptId);

    assertEquals("EUR", scriptExecution.getVariable("currency").getValue());
  }
}

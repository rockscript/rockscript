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

import io.rockscript.activity.ActivityInput;
import io.rockscript.activity.ActivityOutput;
import io.rockscript.engine.JsonObject;
import io.rockscript.engine.Script;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.test.ScriptExecutionComparator;
import io.rockscript.test.ScriptTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;
import static org.junit.Assert.assertEquals;

public class SerializationTest extends ScriptTest {

  protected static Logger log = LoggerFactory.getLogger(SerializationTest.class);

  List<ActivityInput> activityInputs = new ArrayList<>();

  @Override
  protected ScriptService initializeScriptService() {
    // This ensures that each test will get a new ScriptService
    // so that the tests can customize the import resolver without
    // polluting any cached script services.
    return new TestConfiguration().build();
  }

  @Test
  public void testSerialization() {
    scriptService.getConfiguration().getImportResolver().add(
        "helloService", new JsonObject()
            .put("hi", input -> {
              return ActivityOutput.endFunction(input.getArg(0)+" world");
            })
            .put("world", input -> {
              activityInputs.add(input);
              return ActivityOutput.waitForFunctionToCompleteAsync();
            }));

    Script script = deployScript(
        "var helloService = system.import('helloService'); \n" +
        "var response = helloService.hi(system.input.message); \n" +
        "helloService.world(response);");

    ScriptExecution scriptExecution = startScriptExecution(script, hashMap(
        entry("message", "hello")
    ));

    String scriptExecutionId = scriptExecution.getId();

    new ScriptExecutionComparator()
      .assertEquals(scriptExecution, reloadScriptExecution(scriptExecutionId));

    ActivityInput activityInput = activityInputs.get(0);

    assertEquals("hello world", activityInput.getArg(0));

    scriptExecution = scriptService.endActivity(activityInput.getContinuationReference());

    new ScriptExecutionComparator()
      .assertEquals(scriptExecution, reloadScriptExecution(scriptExecutionId));
  }

  private ScriptExecution reloadScriptExecution(String scriptExecutionId) {
    return scriptService
      .getConfiguration()
      .getEventStore()
      .findScriptExecutionById(scriptExecutionId);
  }
}

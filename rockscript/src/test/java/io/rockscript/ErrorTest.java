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

import io.rockscript.engine.Script;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.engine.ScriptService;
import io.rockscript.engine.TestConfiguration;
import io.rockscript.test.ScriptTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorTest extends ScriptTest {

  protected static Logger log = LoggerFactory.getLogger(ErrorTest.class);

  @Override
  protected ScriptService initializeScriptService() {
    // This ensures that each test will get a new ScriptService
    // so that the tests can customize the import resolver without
    // polluting any cached script services.
    return new TestConfiguration().build();
  }

  @Test
  public void testAsynchronousActivity() {
    getConfiguration().getImportResolver().createImport("problematicService")
      .put("buzzz", input -> {
        throw new RuntimeException("buzzz");
      });

    Script script = deployScript(
      "var problematicService = system.import('problematicService'); \n" +
      "problematicService.buzzz(); ");

    ScriptExecution scriptExecution = startScriptExecution(script);

    getConfiguration().getEventStore().getEvents().forEach(e-> log.debug(e.toString()));
  }
}

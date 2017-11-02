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

import io.rockscript.activity.test.TestResult;
import io.rockscript.activity.test.TestResults;
import io.rockscript.api.commands.DeployScriptCommand;
import io.rockscript.api.commands.EngineDeployScriptResponse;
import io.rockscript.api.commands.RunTestsCommand;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.test.ScriptTest;
import io.rockscript.util.Io;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@Ignore // Because requires internet access
public class ExampleTest extends ScriptTest {

  protected static Logger log = LoggerFactory.getLogger(ExampleTest.class);

  @Test
  public void testApproval() throws Exception {
    String scriptId = deployScriptResource("../docs/examples/approvals/create-approval.rs")
      .getId();

    ScriptExecution scriptExecution = startScriptExecution(scriptId);

    EngineScriptExecution engineScriptExecution = getConfiguration().getEventStore()
      .findScriptExecutionById(scriptExecution.getId());

    assertNotNull(engineScriptExecution);
  }

  @Test
  public void testTrainsTestOk() throws Exception {
    deployScriptResource("../docs/examples/test/list-trains.rs");
    deployScriptResource("../docs/examples/test/list-trains-test-ok.rst");
    deployScriptResource("../docs/examples/test/list-trains-test-nok.rst");

    TestResults testResults = commandExecutorService.execute(new RunTestsCommand());

    TestResult okTestResult = testResults.findTestResult("../docs/examples/test/list-trains-test-ok.rst");
    assertNull(okTestResult.getErrors());

    TestResult nokTestResult = testResults.findTestResult("../docs/examples/test/list-trains-test-nok.rst");

    log.debug("Events:");
    nokTestResult.getEvents().forEach(e->log.debug(e.toString()));
    log.debug("Errors:");
    nokTestResult.getErrors().forEach(e->log.debug(e.toString()));
    assertEquals(2, testResults.get(0).getErrors().size());
  }

  private EngineDeployScriptResponse deployScriptResource(String fileName) throws FileNotFoundException {
    File file = new File(fileName);
    String scriptText = Io.toString(new FileInputStream(file));
    return commandExecutorService.execute(new DeployScriptCommand()
        .scriptName(fileName)
        .scriptText(scriptText))
      .throwIfErrors();
  }
}

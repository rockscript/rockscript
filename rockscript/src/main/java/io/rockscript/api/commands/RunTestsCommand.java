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
package io.rockscript.api.commands;

import io.rockscript.activity.test.*;
import io.rockscript.api.Command;
import io.rockscript.api.CommandExecutorService;
import io.rockscript.api.model.ScriptExecution;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.Configuration;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.engine.impl.ScriptStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RunTestsCommand extends Command<TestResults> {

  transient static Logger log = LoggerFactory.getLogger(RunTestsCommand.class);

  protected String tests = ".*\\.rst";

  @Override
  public TestResults execute(Configuration configuration) {
    TestResults testResults = new TestResults();

    ScriptStore scriptStore = configuration.getScriptStore();
    List<ScriptVersion> scriptVersions = scriptStore.findLatestScriptVersionsByNamePattern(tests);
    for (ScriptVersion scriptVersion: scriptVersions) {
      TestResult testResult = runTest(configuration, scriptVersion);
      testResults.add(testResult);
    }
    return testResults;
  }

  private TestResult runTest(Configuration engineConfiguration, ScriptVersion scriptVersion) {
    TestResult testResult = new TestResult(scriptVersion.getName());
    TestImportObject testImportObject = new TestImportObject(testResult);
    Configuration testConfiguration = new TestRunConfiguration(engineConfiguration, testImportObject, testResult);
    CommandExecutorService testCommandExecutorService = testConfiguration.build();
    testImportObject.setCommandExecutorService(testCommandExecutorService);
    try {
      String scriptVersionId = scriptVersion.getId();
      EngineStartScriptExecutionResponse response = testCommandExecutorService.execute(new StartScriptExecutionCommand()
        .scriptVersionId(scriptVersionId));
      ScriptExecution scriptExecution = response.getScriptExecution();
      EngineScriptExecution engineScriptExecution = response.getEngineScriptExecution();

      TestScriptExecution testScriptExecution = new TestScriptExecution();
      testScriptExecution.setId("test-"+scriptExecution.getId());
      testScriptExecution.setScriptName(engineScriptExecution.getEngineScript().getScriptVersion().getName());
      String scriptText = engineScriptExecution.getEngineScript().getScriptVersion().getText();
      log.debug("Script text: "+scriptText);
      testScriptExecution.setScriptText(scriptText);
      testScriptExecution.setStart(scriptExecution.getStart());
      testScriptExecution.setEnd(scriptExecution.getEnd());

      testResult.setScriptExecution(testScriptExecution);

    } catch (Throwable t) {
      testResult.addError(new TestError(t));
    }
    return testResult;
  }

  public String getTests() {
    return this.tests;
  }
  public void setTests(String tests) {
    this.tests = tests;
  }
  public RunTestsCommand tests(String tests) {
    this.tests = tests;
    return this;
  }
}

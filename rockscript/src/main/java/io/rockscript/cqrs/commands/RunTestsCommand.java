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
package io.rockscript.cqrs.commands;

import io.rockscript.activity.test.*;
import io.rockscript.engine.Configuration;
import io.rockscript.engine.Script;
import io.rockscript.engine.impl.*;
import io.rockscript.cqrs.Command;
import io.rockscript.cqrs.CommandExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RunTestsCommand extends Command<TestResults> {

  transient static Logger log = LoggerFactory.getLogger(RunTestsCommand.class);

  protected String tests = ".*\\.rst";

  public RunTestsCommand() {
  }

  public RunTestsCommand(Configuration configuration) {
    super(configuration);
  }

  @Override
  protected TestResults execute(Configuration configuration) {
    TestResults testResults = new TestResults();

    ScriptStore scriptStore = configuration.getScriptStore();
    List<Script> scriptVersions = scriptStore.findLatestScriptVersionsByNamePattern(tests);
    for (Script scriptVersion: scriptVersions) {
      TestResult testResult = runTest(configuration, scriptVersion);
      testResults.add(testResult);
    }
    return testResults;
  }

  private TestResult runTest(Configuration engineConfiguration, Script script) {
    TestResult testResult = new TestResult(script.getName());
    TestImportObject testImportObject = new TestImportObject(testResult);
    Configuration testConfiguration = new TestRunConfiguration(engineConfiguration, testImportObject, testResult);
    CommandExecutorService testCommandExecutorService = testConfiguration.build();
    testImportObject.setCommandExecutorService(testCommandExecutorService);
    try {
      String scriptId = script.getId();
      testCommandExecutorService.execute(new StartScriptExecutionCommand()
        .scriptId(scriptId));
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

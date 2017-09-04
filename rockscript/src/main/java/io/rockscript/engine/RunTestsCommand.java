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
package io.rockscript.engine;

import io.rockscript.activity.test.TestImportObject;
import io.rockscript.activity.test.TestResult;
import io.rockscript.activity.test.TestResults;
import io.rockscript.activity.test.TestRunConfiguration;
import io.rockscript.engine.impl.ErrorMessage;
import io.rockscript.engine.impl.ScriptStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

public class RunTestsCommand extends CommandImpl<TestResults> {

  static Logger log = LoggerFactory.getLogger(RunTestsCommand.class);

  protected String tests;

  public RunTestsCommand(Configuration configuration) {
    super(configuration);
  }

  @Override
  protected TestResults execute(Configuration configuration) {
    TestResults testResults = new TestResults();

    if (!Pattern.matches("[\\w\\* -\\.]*", tests)) {
      throw new EngineException("Invalid tests pattern: "+tests);
    }
    tests = tests.replace("*", ".*");

    ScriptStore scriptStore = configuration.getScriptStore();
    List<Script> scriptVersions = scriptStore.findLatestScriptVersionsByNamePattern(tests);
    for (Script scriptVersion: scriptVersions) {
      TestResult testResult = runTest(configuration, scriptVersion);
      testResults.add(testResult);
    }
    return testResults;
  }

  private TestResult runTest(Configuration engineConfiguration, Script scriptVersion) {
    TestResult testResult = new TestResult();
    TestImportObject testImportObject = new TestImportObject(testResult);
    Configuration testConfiguration = new TestRunConfiguration(engineConfiguration, testImportObject, testResult);
    ScriptService testScriptService = testConfiguration.build();
    testImportObject.setScriptService(testScriptService);
    try {
      String scriptId = scriptVersion.getId();
      testScriptService
        .newStartScriptExecutionCommand()
          .scriptId(scriptId)
          .execute();
    } catch (Throwable t) {
      testResult.setError(new ErrorMessage(t));
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

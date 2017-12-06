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
package io.rockscript.service.test;

import io.rockscript.Engine;
import io.rockscript.service.ServiceFunctionInput;
import io.rockscript.service.ServiceFunctionOutput;
import io.rockscript.service.ImportObject;
import io.rockscript.service.ImportProvider;
import io.rockscript.api.commands.ScriptExecutionResponse;
import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.api.model.Script;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.ScriptStore;

import java.util.List;
import java.util.regex.Pattern;

public class TestImportObject extends ImportObject implements ImportProvider {

  private TestResult testResult;
  private Engine engine;

  public TestImportObject(TestResult testResult) {
    super("rockscript.io/test");
    this.testResult = testResult;
    put("start", functionInput -> {
      String scriptNamePattern = functionInput.getArgProperty("script");
      Object startInput = functionInput.getArgProperty("input");
      ScriptExecutionResponse response = null;
      try {
        String scriptVersionId = findLatestScriptVersionIdByScriptNamePattern(scriptNamePattern, functionInput);

        if (scriptVersionId==null) {
          return ServiceFunctionOutput.error("No script matched name patter " + scriptNamePattern);
        }

        response = new StartScriptExecutionCommand()
            .scriptVersionId(scriptVersionId)
            .input(startInput)
            .execute(engine);

        if (response.getErrorEvent()==null) {
          return ServiceFunctionOutput.endFunction(response.getScriptExecution());
        } else {
          return ServiceFunctionOutput.error("Script start failed: " + response.getErrorEvent().getError());
        }
      } catch (Exception e) {
        return ServiceFunctionOutput.error("Test execution error: " + e.getMessage());
      }
    });
    put("assertEquals", input -> {
      Object actual = input.getArg(0);
      Object expected = input.getArg(1);
      if (!equal(actual, expected)) {
        return ServiceFunctionOutput.error("Expected " + expected + ", but was " + actual);
      }
      return ServiceFunctionOutput.endFunction();
    }, "actual", "expected");
  }

  private String findLatestScriptVersionIdByScriptNamePattern(String scriptNamePattern, ServiceFunctionInput input) {
    ScriptStore scriptStore = input.getExecution()
      .getScriptExecution()
      .getEngineScript()
      .getEngine()
      .getScriptStore();

    List<Script> scripts = scriptStore.getScripts();
    for (Script script: scripts) {
      if (Pattern.matches(scriptNamePattern, script.getName())) {
        List<ScriptVersion> scriptVersions = script.getScriptVersions();
        if (scriptVersions!=null && !scriptVersions.isEmpty()) {
          ScriptVersion scriptVersion = scriptVersions.get(scriptVersions.size() - 1);
          return scriptVersion.getId();
        }
      }
    }
    return null;
  }

  private boolean equal(Object a, Object b) {
    if (a==null && b==null) {
      return true;
    }
    if (a!=null) {
      return a.equals(b);
    }
    return b.equals(a);
  }

  public TestResult getTestResult() {
    return testResult;
  }

  @Override
  public ImportObject getImportObject() {
    return this;
  }

  public void setEngine(Engine engine) {
    this.engine = engine;
  }
}

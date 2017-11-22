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
package io.rockscript.activity.test;

import io.rockscript.Engine;
import io.rockscript.activity.ActivityInput;
import io.rockscript.activity.ActivityOutput;
import io.rockscript.activity.ImportObject;
import io.rockscript.activity.ImportProvider;
import io.rockscript.api.commands.EngineStartScriptExecutionResponse;
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
    put("start", activityInput -> {
      String scriptNamePattern = activityInput.getArgProperty("script");
      Object input = activityInput.getArgProperty("input");
      EngineStartScriptExecutionResponse response = null;
      try {
        String scriptVersionId = findLatestScriptVersionIdByScriptNamePattern(scriptNamePattern, activityInput);

        if (scriptVersionId==null) {
          return ActivityOutput.error("No script matched name patter "+scriptNamePattern);
        }

        response = new StartScriptExecutionCommand()
            .scriptVersionId(scriptVersionId)
            .input(input)
            .execute(engine);

        if (response.getErrorEvent()==null) {
          return ActivityOutput.endActivity(response.getScriptExecution());
        } else {
          return ActivityOutput.error("ScriptVersion start failed: "+response.getErrorEvent().getError());
        }
      } catch (Exception e) {
        return ActivityOutput.error("Test execution error: "+e.getMessage());
      }
    });
    put("assertEquals", activityInput -> {
      Object actual = activityInput.getArg(0);
      Object expected = activityInput.getArg(1);
      if (!equal(actual, expected)) {
        return ActivityOutput.error("Expected "+expected+", but was "+actual);
      }
      return ActivityOutput.endActivity();
    }, "actual", "expected");
  }

  private String findLatestScriptVersionIdByScriptNamePattern(String scriptNamePattern, ActivityInput activityInput) {
    ScriptStore scriptStore = activityInput.getExecution()
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

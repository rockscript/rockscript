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
import io.rockscript.engine.impl.MonitoringExecutor;
import io.rockscript.engine.impl.ScriptStore;
import io.rockscript.test.TestExecutor;
import io.rockscript.test.TestJobExecutor;

public class TestRunEngine extends Engine {

  public TestRunEngine(Engine engine, TestImportObject testImportObject, TestResult testResult) {
    super(engine);
    getImportResolver().add(testImportObject);
    this.scriptStore = new ScriptStore(this, engine.getScriptStore());
    this.eventDispatcher = new TestEventLogger(this, testResult, eventDispatcher);
    this.executor = MonitoringExecutor.createTest(getEngineLogStore());
    this.jobExecutor = new TestJobExecutor(this);
  }
}

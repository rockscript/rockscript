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
 */

package io.rockscript;

import java.io.File;

import io.rockscript.engine.*;
import io.rockscript.test.TestEngine;
import org.junit.Before;

import static io.rockscript.engine.ActionResponse.endFunction;

/**
 * Tests to run sample code used in the README.
 *
 * If you make changes, update the README with the updated test method body.
 */
public class ReadmeTest {

  EngineImpl engine;

  @Before
  public void createTestEngine() {
    engine = new TestEngine();
    ImportResolver importResolver = engine.getServiceLocator().getImportResolver();
    JsonObject httpService = new JsonObject()
        .put("get", functionInput -> ActionResponse.endFunction());
    importResolver.add("core/http", httpService);
  }

//  @Test
  public void testUsage() throws Exception {
    String scriptId = engine.deployScript(new File("resources/hello.script"));
    ScriptExecution execution = engine.startScriptExecutionImpl(scriptId);
  }
}

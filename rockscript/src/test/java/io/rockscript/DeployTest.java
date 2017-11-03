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

import io.rockscript.api.commands.SaveScriptVersionCommand;
import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.test.ScriptTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class DeployTest extends ScriptTest {

  protected static Logger log = LoggerFactory.getLogger(DeployTest.class);

  @Test
  public void testSaveVsDeploy() {
    ScriptVersion scriptVersion = execute(new SaveScriptVersionCommand()
        .scriptText("var a = 1;"))
      .throwIfErrors();

    scriptVersion = execute(new SaveScriptVersionCommand()
        .scriptText("var a = 2;")
        .activate())
      .throwIfErrors();

    scriptVersion = execute(new SaveScriptVersionCommand()
        .scriptText("var a = 3;"))
      .throwIfErrors();

    EngineScriptExecution scriptExecution = execute(new StartScriptExecutionCommand()
      .scriptId(scriptVersion.getScriptId()))
      .getEngineScriptExecution();

    assertEquals(2d, scriptExecution.getVariable("a").getValue());
  }

}

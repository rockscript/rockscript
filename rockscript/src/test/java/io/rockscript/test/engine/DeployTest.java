/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.rockscript.test.engine;

import io.rockscript.api.commands.SaveScriptVersionCommand;
import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.EngineScriptExecution;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class DeployTest extends AbstractEngineTest {

  protected static Logger log = LoggerFactory.getLogger(DeployTest.class);

  @Test
  public void testSaveVsDeploy() {
    ScriptVersion scriptVersion = new SaveScriptVersionCommand()
        .scriptText("var a = 1;")
        .execute(engine)
        .throwIfErrors();

    scriptVersion = new SaveScriptVersionCommand()
        .scriptText("var a = 2;")
        .activate()
        .execute(engine)
        .throwIfErrors();

    assertEquals(Boolean.TRUE, scriptVersion.getActive());

    scriptVersion = new SaveScriptVersionCommand()
        .scriptText("var a = 3;")
        .execute(engine)
        .throwIfErrors();

    EngineScriptExecution scriptExecution = new StartScriptExecutionCommand()
      .scriptId(scriptVersion.getScriptId())
      .execute(engine)
      .getEngineScriptExecution();

    assertEquals(2d, scriptExecution.getVariable("a").getValue());
  }

}

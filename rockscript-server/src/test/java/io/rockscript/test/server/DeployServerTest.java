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
package io.rockscript.test.server;

import io.rockscript.api.commands.DeployScriptVersionCommand;
import io.rockscript.api.model.ParseError;
import io.rockscript.api.model.ScriptVersion;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

public class DeployServerTest extends AbstractServerTest {

  protected static Logger log = LoggerFactory.getLogger(DeployServerTest.class);

  @Test
  public void testDeployOk() {
    ScriptVersion saveScriptVersionResponse = newPost("/command")
      .bodyJson(new DeployScriptVersionCommand()
          .scriptName("Test script")
          .scriptText("var a=0;"))
      .execute(ScriptVersion.class)
      .assertStatusOk()
      .getBody();

    assertNotNull(saveScriptVersionResponse.getId());
    assertEquals((Integer) 1, saveScriptVersionResponse.getVersion());
    assertEquals("Test script", saveScriptVersionResponse.getName());
    assertNull(saveScriptVersionResponse.getErrors());
  }

  @Test
  public void testDeploySyntaxError() {
    ScriptVersion saveScriptVersionResponse = newPost("/command")
      .bodyJson(new DeployScriptVersionCommand()
        .scriptText("\n\ninvalid script"))
      .execute(ScriptVersion.class)
      .assertStatusOk()
      .getBody();

    List<ParseError> errors = saveScriptVersionResponse.getErrors();
    assertEquals(1, errors.size());
    ParseError error = errors.get(0);
    assertEquals(3, error.getLine());
    assertEquals(8, error.getColumn());
    assertEquals("no viable alternative at input 'script'", error.getMessage());
  }

  @Test
  public void testDeployParseError() {
    ScriptVersion saveScriptVersionResponse = newPost("/command")
      .bodyJson(new DeployScriptVersionCommand()
        .scriptText("\n\nvar a = new Object();"))
      .execute(ScriptVersion.class)
      .assertStatusOk()
      .getBody();

    List<ParseError> errors = saveScriptVersionResponse.getErrors();
    assertEquals(1, errors.size());
    ParseError error = errors.get(0);
    assertEquals(3, error.getLine());
    assertEquals(8, error.getColumn());
    assertEquals("Unsupported singleExpression: newObject() NewExpressionContext", error.getMessage());
  }

}

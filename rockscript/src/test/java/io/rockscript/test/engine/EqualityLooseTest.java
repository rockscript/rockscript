/*
 * Copyright (c) 2018 RockScript.io.
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

import io.rockscript.api.model.ScriptExecution;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.Literal;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EqualityLooseTest extends AbstractEngineTest {

  @Test
  public void testLooseEqualOfNumbers() {
    ScriptVersion scriptVersion = deployScript(
      "var result = 1 == system.input;");
    ScriptExecution scriptExecution = startScriptExecution(scriptVersion, 1);
    assertEquals(true, scriptExecution.getVariable("result"));

    scriptExecution = startScriptExecution(scriptVersion, 2);
    assertEquals(false, scriptExecution.getVariable("result"));
  }

  @Test
  public void testLooseStrings() {
    ScriptVersion scriptVersion = deployScript(
      "var result = 'hello' == system.input;");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion, "hello");
    assertEquals(true, scriptExecution.getVariable("result"));

    scriptExecution = startScriptExecution(scriptVersion, "world");
    assertEquals(false, scriptExecution.getVariable("result"));
  }

  @Test
  public void testLooseStringsNumberConversion() {
    ScriptVersion scriptVersion = deployScript(
      "var result = '12' == system.input;");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion, 12);
    assertEquals(true, scriptExecution.getVariable("result"));

    scriptExecution = startScriptExecution(scriptVersion, 13);
    assertEquals(false, scriptExecution.getVariable("result"));
  }

}

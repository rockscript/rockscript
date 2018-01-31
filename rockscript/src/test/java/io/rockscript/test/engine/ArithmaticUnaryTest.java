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

import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.Literal;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArithmaticUnaryTest extends AbstractEngineTest {

  @Test
  public void testPostIncrement() {
    ScriptVersion scriptVersion = deployScript(
      "var i = 0; \n" +
      "var result = i++");
    assertEquals(1d, startScriptExecution(scriptVersion).getVariable("i"));
    assertEquals(0d, startScriptExecution(scriptVersion).getVariable("result"));
  }

  @Test
  public void testPreIncrement() {
    ScriptVersion scriptVersion = deployScript(
      "var i = 0; \n" +
      "var result = ++i");
    assertEquals(1d, startScriptExecution(scriptVersion).getVariable("i"));
    assertEquals(1d, startScriptExecution(scriptVersion).getVariable("result"));
  }

  @Test
  public void testPostDecrement() {
    ScriptVersion scriptVersion = deployScript(
      "var i = 0; \n" +
      "var result = i--");
    assertEquals(-1d, startScriptExecution(scriptVersion).getVariable("i"));
    assertEquals(0d, startScriptExecution(scriptVersion).getVariable("result"));
  }

  @Test
  public void testPreDecrement() {
    ScriptVersion scriptVersion = deployScript(
      "var i = 0; \n" +
      "var result = --i");
    assertEquals(-1d, startScriptExecution(scriptVersion).getVariable("i"));
    assertEquals(-1d, startScriptExecution(scriptVersion).getVariable("result"));
  }
}

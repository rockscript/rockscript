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
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.rockscript.test.TesterImportObject.CONTEXT_KEY_RETURN_VALUES;
import static org.junit.Assert.assertEquals;

public class AssignmentTest extends AbstractEngineTest {

  @Test
  public void testLiteralAssignment() {
    ScriptVersion scriptVersion = deployScript(
      "var result; \n" +
      "result = 'hello';");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion);
    assertEquals("hello", scriptExecution.getVariable("result"));
  }

  @Test
  public void testLeftHandMemberDotDereference() {
    ScriptVersion scriptVersion = deployScript(
      "var result = {}; \n" +
      "result.greeting = 'hello';");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion);
    @SuppressWarnings("unchecked")
    Map<String,Object> result = (Map<String,Object>) scriptExecution.getVariable("result");
    assertEquals("hello", result.get("greeting"));
  }

  @Test
  public void testRightHandFunction() {
    ScriptVersion scriptVersion = deployScript(
      "var tester = system.import('tester'); \n" +
      "var result; \n" +
      "result = tester.invoke();");

    addTesterInvokeReturnValue("hello");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion);
    assertEquals("hello", scriptExecution.getVariable("result"));
  }
}

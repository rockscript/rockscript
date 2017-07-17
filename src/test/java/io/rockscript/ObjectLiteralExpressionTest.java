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

import java.util.*;

import io.rockscript.engine.*;
import io.rockscript.test.TestEngine;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.rockscript.engine.ActionResponse.endFunction;
import static org.junit.Assert.assertEquals;

public class ObjectLiteralExpressionTest {

  protected static Logger log = LoggerFactory.getLogger(ObjectLiteralExpressionTest.class);

  EngineImpl engine = createTestEngine();
  List<Object> capturedValues = new ArrayList<>();

  public EngineImpl createTestEngine() {
    TestEngine engine = new TestEngine();
    ImportResolver importResolver = engine.getServiceLocator().getImportResolver();
    JsonObject helloService = new JsonObject()
      .put("assertLiteralValue", functionInput->{
          capturedValues.add(functionInput.getArgs().get(0));
          return ActionResponse.endFunction();});
    importResolver.add("example.com/assert", helloService);
    return engine;
  }

  @Test
  public void testSimpleStaticObjectLiteralExpressionWithIdentifier() {
    String scriptId = engine.deployScript(
        "var o = {msg: 'hello'};");

    ScriptExecution scriptExecution = engine.startScriptExecutionImpl(scriptId);
    Map<String,Object> o = (Map<String, Object>) scriptExecution.getVariable("o").getValue();
    assertEquals("hello", o.get("msg"));
  }

  @Test
  public void testSimpleStaticObjectLiteralExpressionWithPropertyString() {
    String scriptId = engine.deployScript(
      "var o = {'m s g': 'hello'};");

    ScriptExecution scriptExecution = engine.startScriptExecutionImpl(scriptId);
    Map<String,Object> o = (Map<String, Object>) scriptExecution.getVariable("o").getValue();
    assertEquals("hello", o.get("m s g"));
  }

  @Test
  public void testDynamicObjectLiteralExpression() {
    String scriptId = engine.deployScript(
      "var greeting = 'hello'; \n"+
      "var o = {msg: greeting}; ");

    ScriptExecution scriptExecution = engine.startScriptExecutionImpl(scriptId);
    Map<String,Object> o = (Map<String, Object>) scriptExecution.getVariable("o").getValue();
    assertEquals("hello", o.get("msg"));
  }

}

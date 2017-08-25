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

import io.rockscript.activity.ActivityOutput;
import io.rockscript.engine.ImportResolver;
import io.rockscript.engine.JsonObject;
import io.rockscript.engine.ScriptExecution;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ObjectLiteralExpressionTest {

  protected static Logger log = LoggerFactory.getLogger(ObjectLiteralExpressionTest.class);

  ScriptService scriptService = createTestEngine();
  List<Object> capturedValues = new ArrayList<>();

  public ScriptService createTestEngine() {
    ScriptService engine = new TestConfiguration().build();
    ImportResolver importResolver = engine.getConfiguration().getImportResolver();
    JsonObject helloService = new JsonObject()
      .put("assertLiteralValue", input -> {
          capturedValues.add(input.getArgs().get(0));
          return ActivityOutput.endFunction();});
    importResolver.add("example.com/assert", helloService);
    return engine;
  }

  @Test
  public void testSimpleStaticObjectLiteralExpressionWithIdentifier() {
    String scriptId = scriptService
      .newDeployScriptCommand()
        .text("var o = {msg: 'hello'};")
        .execute()
      .getId();

    ScriptExecution scriptExecution = scriptService.startScriptExecution(scriptId);
    @SuppressWarnings("unchecked")
    Map<String,Object> o = (Map<String, Object>) scriptExecution.getVariable("o").getValue();
    assertEquals("hello", o.get("msg"));
  }

  @Test
  public void testSimpleStaticObjectLiteralExpressionWithPropertyString() {
    String scriptId = scriptService
      .newDeployScriptCommand()
        .text("var o = {'m s g': 'hello'};")
        .execute()
      .getId();

    ScriptExecution scriptExecution = scriptService.startScriptExecution(scriptId);
    @SuppressWarnings("unchecked")
    Map<String,Object> o = (Map<String, Object>) scriptExecution.getVariable("o").getValue();
    assertEquals("hello", o.get("m s g"));
  }

  @Test
  public void testDynamicObjectLiteralExpression() {
    String scriptId = scriptService
      .newDeployScriptCommand()
        .text("var greeting = 'hello'; \n"+
              "var o = {msg: greeting}; ")
        .execute()
      .getId();

    ScriptExecution scriptExecution = scriptService.startScriptExecution(scriptId);
    @SuppressWarnings("unchecked")
    Map<String,Object> o = (Map<String, Object>) scriptExecution.getVariable("o").getValue();
    assertEquals("hello", o.get("msg"));
  }

}

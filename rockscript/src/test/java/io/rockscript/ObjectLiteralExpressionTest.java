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
import io.rockscript.engine.Script;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.engine.ScriptService;
import io.rockscript.engine.TestConfiguration;
import io.rockscript.test.ScriptTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ObjectLiteralExpressionTest extends ScriptTest {

  protected static Logger log = LoggerFactory.getLogger(ObjectLiteralExpressionTest.class);

  List<Object> capturedValues = new ArrayList<>();

  @Override
  protected ScriptService initializeScriptService() {
    TestConfiguration configuration = new TestConfiguration();
    configuration.getImportResolver().createImport("example.com/assert")
      .put("assertLiteralValue", input -> {
        capturedValues.add(input.getArgs().get(0));
        return ActivityOutput.endActivity();});
    return configuration.build();
  }

  @Test
  public void testSimpleStaticObjectLiteralExpressionWithIdentifier() {
    Script script = deployScript(
        "var o = {msg: 'hello'};");

    ScriptExecution scriptExecution = startScriptExecution(script);
    @SuppressWarnings("unchecked")
    Map<String,Object> o = (Map<String, Object>) scriptExecution.getVariable("o");
    assertEquals("hello", o.get("msg"));
  }

  @Test
  public void testSimpleStaticObjectLiteralExpressionWithPropertyString() {
    Script script = deployScript(
        "var o = {'m s g': 'hello'};");

    ScriptExecution scriptExecution = startScriptExecution(script);
    @SuppressWarnings("unchecked")
    Map<String,Object> o = (Map<String, Object>) scriptExecution.getVariable("o");
    assertEquals("hello", o.get("m s g"));
  }

  @Test
  public void testDynamicObjectLiteralExpression() {
    Script script = deployScript(
        "var greeting = 'hello'; \n"+
        "var o = {msg: greeting}; ");

    ScriptExecution scriptExecution = startScriptExecution(script);
    @SuppressWarnings("unchecked")
    Map<String,Object> o = (Map<String, Object>) scriptExecution.getVariable("o");
    assertEquals("hello", o.get("msg"));
  }

}

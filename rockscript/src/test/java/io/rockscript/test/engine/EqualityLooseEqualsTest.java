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
import io.rockscript.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;
import static org.junit.Assert.assertEquals;

public class EqualityLooseEqualsTest extends AbstractEngineTest {

  ScriptVersion scriptVersion = null;

  @Before
  @Override
  public void setUp() {
    super.setUp();
    scriptVersion = deployScript("var result = system.input.left == system.input.right;");
  }

  protected void assertLooseEqual(Object left, Object right) {
    assertEquals(true, getResult(left, right));
    assertEquals(true, getResult(right, left));
  }

  protected void assertLooseNotEqual(Object left, Object right) {
    assertEquals(false, getResult(left, right));
    assertEquals(false, getResult(right, left));
  }

  private boolean getResult(Object left, Object right) {
    Map<String,Object> input = hashMap(entry("left", left), entry("right", right));
    ScriptExecution scriptExecution = startScriptExecution(scriptVersion, input);
    return (boolean) scriptExecution.getVariable("result");
  }


  @Test
  public void testLooseEquals() {

    // https://developer.mozilla.org/nl/docs/Web/JavaScript/Equality_comparisons_and_sameness#A_model_for_understanding_equality_comparisons

    assertLooseEqual(Literal.UNDEFINED, Literal.UNDEFINED);
    assertLooseEqual(null, null);
    assertLooseEqual(true, true);
    assertLooseEqual(false, false);
    assertLooseEqual("foo", "foo");

    HashMap<String, String> fooBarObject = hashMap(entry("foo", "bar"));
    assertLooseEqual(fooBarObject, fooBarObject);

    assertLooseEqual(0, 0);
    // assertLooseEqual(+0, -0); // This impl doesn't distinct +0 and -0 yet...
    assertLooseEqual(0, false);
    assertLooseEqual("", false);
    assertLooseEqual("", 0);
    assertLooseEqual("0", 0);
    assertLooseEqual("17", 17);
    assertLooseEqual(Arrays.asList(1,2), "1,2");
    // assertLooseEqual(new String("foo"), "foo"); // This impl doesn't support string constructor yet
    assertLooseEqual(null, Literal.UNDEFINED);

    assertLooseNotEqual(null, false);
    assertLooseNotEqual(Literal.UNDEFINED, false);
    assertLooseNotEqual(hashMap(entry("foo", "bar")), hashMap(entry("foo", "bar")));
    assertLooseNotEqual(0, null);
    assertLooseNotEqual(0, Literal.NAN);
    assertLooseNotEqual("foo", Literal.NAN);
    assertLooseNotEqual(Literal.NAN, Literal.NAN);

    // Extra test based on Node.js

    List<Object> fooBarArray = Lists.of("foo", "bar");
    assertLooseEqual(fooBarArray, fooBarArray);
    assertLooseNotEqual(Lists.of("foo", "bar"), Lists.of("foo", "bar"));
    assertLooseEqual(Arrays.asList(1,"str",true,null,Literal.UNDEFINED,hashMap(entry("a", 1)),Arrays.asList("one","two")),
      "1,str,true,,,[object Object],one,two");
  }

}

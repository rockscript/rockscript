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

public class EqualityStrictEqualsTest extends AbstractEngineTest {

  ScriptVersion scriptVersion = null;

  @Before
  @Override
  public void setUp() {
    super.setUp();
    scriptVersion = deployScript("var result = system.input.left === system.input.right;");
  }

  protected void assertStrictEqual(Object left, Object right) {
    assertEquals(true, getResult(left, right));
    assertEquals(true, getResult(right, left));
  }

  protected void assertStrictNotEqual(Object left, Object right) {
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

    assertStrictEqual(Literal.UNDEFINED, Literal.UNDEFINED);
    assertStrictEqual(null, null);
    assertStrictEqual(true, true);
    assertStrictEqual(false, false);
    assertStrictEqual("foo", "foo");

    HashMap<String, String> fooBarObject = hashMap(entry("foo", "bar"));
    assertStrictEqual(fooBarObject, fooBarObject);

    assertStrictEqual(0, 0);
    // assertLooseEqual(+0, -0); // This impl doesn't distinct +0 and -0 yet...

    assertStrictNotEqual(0, false);
    assertStrictNotEqual("", false);
    assertStrictNotEqual("", 0);
    assertStrictNotEqual("0", 0);
    assertStrictNotEqual("17", 17);
    assertStrictNotEqual(Arrays.asList(1,2), "1,2");
    // assertLooseNotEqual(new String("foo"), "foo"); // This impl doesn't support string constructor yet
    assertStrictNotEqual(null, Literal.UNDEFINED);

    assertStrictNotEqual(null, false);
    assertStrictNotEqual(Literal.UNDEFINED, false);
    assertStrictNotEqual(hashMap(entry("foo", "bar")), hashMap(entry("foo", "bar")));
    assertStrictNotEqual(0, null);
    assertStrictNotEqual(0, Literal.NAN);
    assertStrictNotEqual("foo", Literal.NAN);
    assertStrictNotEqual(Literal.NAN, Literal.NAN);

    // Extra test based on Node.js

    List<Object> fooBarArray = Lists.of("foo", "bar");
    assertStrictEqual(fooBarArray, fooBarArray);
    assertStrictNotEqual(Lists.of("foo", "bar"), Lists.of("foo", "bar"));
  }

}

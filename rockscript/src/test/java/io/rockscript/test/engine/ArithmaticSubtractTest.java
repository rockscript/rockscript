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

public class ArithmaticSubtractTest extends AbstractEngineTest {

  /*
  Test was generated from (and based on) node.js code:

  subtract(1, 2);
  subtract(1, 'y');
  subtract(1, true);
  subtract(1, null);
  subtract(1, undefined);
  subtract(1, {'a':1});
  subtract(1, ['a',1]);
  console.log();

  subtract('x', 2);
  subtract('x', 'y');
  subtract('x', true);
  subtract('x', null);
  subtract('x', undefined);
  subtract('x', {'a':1});
  subtract('x', ['a',1]);
  console.log();

  subtract(true, 2);
  subtract(true, 'y');
  subtract(true, true);
  subtract(true, null);
  subtract(true, undefined);
  subtract(true, {'a':1});
  subtract(true, ['a',1]);
  console.log();

  subtract(null, 2);
  subtract(null, 'y');
  subtract(null, true);
  subtract(null, null);
  subtract(null, undefined);
  subtract(null, {'a':1});
  subtract(null, ['a',1]);
  console.log();

  subtract(undefined, 2);
  subtract(undefined, 'y');
  subtract(undefined, true);
  subtract(undefined, null);
  subtract(undefined, undefined);
  subtract(undefined, {'a':1});
  subtract(undefined, ['a',1]);
  console.log();

  subtract({'a':1}, 2);
  subtract({'a':1}, 'y');
  subtract({'a':1}, true);
  subtract({'a':1}, null);
  subtract({'a':1}, undefined);
  subtract({'a':1}, {'a':1});
  subtract({'a':1}, ['a',1]);
  console.log();

  subtract(['a',1], 2);
  subtract(['a',1], 'y');
  subtract(['a',1], true);
  subtract(['a',1], null);
  subtract(['a',1], undefined);
  subtract(['a',1], {'a':1});
  subtract(['a',1], ['a',1]);

  function subtract(l,r) {
    var result = l-r;
    console.log('    assertSubtract("'+result+'", "'+valueToString(l)+'-'+valueToString(r)+'");');
  }

  function valueToString(o) {
    return ('string'===typeof o? '\''+o+'\'' : 'object'===typeof o || 'array'===typeof o ? JSON.stringify(o).replace(new RegExp('"', 'g'), "'") :o );
  }
  */
  @Test
  public void testArithmaticSubtractions() {
    assertSubtract(-1d, "1-2");
    assertSubtract(Literal.NAN, "1-'y'");
    assertSubtract(0d, "1-true");
    assertSubtract(1d, "1-null");
    assertSubtract(Literal.NAN, "1-undefined");
    assertSubtract(Literal.NAN, "1-{'a':1}");
    assertSubtract(Literal.NAN, "1-['a',1]");

    assertSubtract(Literal.NAN, "'x'-2");
    assertSubtract(Literal.NAN, "'x'-'y'");
    assertSubtract(Literal.NAN, "'x'-true");
    assertSubtract(Literal.NAN, "'x'-null");
    assertSubtract(Literal.NAN, "'x'-undefined");
    assertSubtract(Literal.NAN, "'x'-{'a':1}");
    assertSubtract(Literal.NAN, "'x'-['a',1]");

    assertSubtract(-1d, "true-2");
    assertSubtract(Literal.NAN, "true-'y'");
    assertSubtract(0d, "true-true");
    assertSubtract(1d, "true-null");
    assertSubtract(Literal.NAN, "true-undefined");
    assertSubtract(Literal.NAN, "true-{'a':1}");
    assertSubtract(Literal.NAN, "true-['a',1]");

    assertSubtract(-2d, "null-2");
    assertSubtract(Literal.NAN, "null-'y'");
    assertSubtract(-1d, "null-true");
    assertSubtract(0d, "null-null");
    assertSubtract(Literal.NAN, "null-undefined");
    assertSubtract(Literal.NAN, "null-{'a':1}");
    assertSubtract(Literal.NAN, "null-['a',1]");

    assertSubtract(Literal.NAN, "undefined-2");
    assertSubtract(Literal.NAN, "undefined-'y'");
    assertSubtract(Literal.NAN, "undefined-true");
    assertSubtract(Literal.NAN, "undefined-null");
    assertSubtract(Literal.NAN, "undefined-undefined");
    assertSubtract(Literal.NAN, "undefined-{'a':1}");
    assertSubtract(Literal.NAN, "undefined-['a',1]");

    assertSubtract(Literal.NAN, "{'a':1}-2");
    assertSubtract(Literal.NAN, "{'a':1}-'y'");
    assertSubtract(Literal.NAN, "{'a':1}-true");
    assertSubtract(Literal.NAN, "{'a':1}-null");
    assertSubtract(Literal.NAN, "{'a':1}-undefined");
    assertSubtract(Literal.NAN, "{'a':1}-{'a':1}");
    assertSubtract(Literal.NAN, "{'a':1}-['a',1]");

    assertSubtract(Literal.NAN, "['a',1]-2");
    assertSubtract(Literal.NAN, "['a',1]-'y'");
    assertSubtract(Literal.NAN, "['a',1]-true");
    assertSubtract(Literal.NAN, "['a',1]-null");
    assertSubtract(Literal.NAN, "['a',1]-undefined");
    assertSubtract(Literal.NAN, "['a',1]-{'a':1}");
    assertSubtract(Literal.NAN, "['a',1]-['a',1]");
  }

  private void assertSubtract(Object expected, String expression) {
    ScriptVersion scriptVersion = deployScript("var result = "+expression+";");
    assertEquals(expected, startScriptExecution(scriptVersion).getVariable("result"));
  }
}

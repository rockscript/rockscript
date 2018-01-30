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

public class ArithmaticDivisionTest extends AbstractEngineTest {

  /*
  Test was generated from (and based on) node.js code:

  divide(1, 2);
  divide(1, 'y');
  divide(1, true);
  divide(1, null);
  divide(1, undefined);
  divide(1, {'a':1});
  divide(1, ['a',1]);
  console.log();

  divide('x', 2);
  divide('x', 'y');
  divide('x', true);
  divide('x', null);
  divide('x', undefined);
  divide('x', {'a':1});
  divide('x', ['a',1]);
  console.log();

  divide(true, 2);
  divide(true, 'y');
  divide(true, true);
  divide(true, null);
  divide(true, undefined);
  divide(true, {'a':1});
  divide(true, ['a',1]);
  console.log();

  divide(null, 2);
  divide(null, 'y');
  divide(null, true);
  divide(null, null);
  divide(null, undefined);
  divide(null, {'a':1});
  divide(null, ['a',1]);
  console.log();

  divide(undefined, 2);
  divide(undefined, 'y');
  divide(undefined, true);
  divide(undefined, null);
  divide(undefined, undefined);
  divide(undefined, {'a':1});
  divide(undefined, ['a',1]);
  console.log();

  divide({'a':1}, 2);
  divide({'a':1}, 'y');
  divide({'a':1}, true);
  divide({'a':1}, null);
  divide({'a':1}, undefined);
  divide({'a':1}, {'a':1});
  divide({'a':1}, ['a',1]);
  console.log();

  divide(['a',1], 2);
  divide(['a',1], 'y');
  divide(['a',1], true);
  divide(['a',1], null);
  divide(['a',1], undefined);
  divide(['a',1], {'a':1});
  divide(['a',1], ['a',1]);

  function divide(l,r) {
    var result = l/r;
    console.log('    assertDivide('+valueToString(result)+', "'+valueToString(l)+'/'+valueToString(r)+'");');
  }

  function valueToString(o) {
    return ('string'===typeof o? '\''+o+'\'' : 'object'===typeof o || 'array'===typeof o ? JSON.stringify(o).replace(new RegExp('"', 'g'), "'") :o );
  }
  */
  @Test
  public void testArithmaticDivisions() {
    assertDivide(0.5d, "1/2");
    assertDivide(Literal.NAN, "1/'y'");
    assertDivide(1d, "1/true");
    assertDivide(Literal.INFINITY, "1/null");
    assertDivide(Literal.NAN, "1/undefined");
    assertDivide(Literal.NAN, "1/{'a':1}");
    assertDivide(Literal.NAN, "1/['a',1]");

    assertDivide(Literal.NAN, "'x'/2");
    assertDivide(Literal.NAN, "'x'/'y'");
    assertDivide(Literal.NAN, "'x'/true");
    assertDivide(Literal.NAN, "'x'/null");
    assertDivide(Literal.NAN, "'x'/undefined");
    assertDivide(Literal.NAN, "'x'/{'a':1}");
    assertDivide(Literal.NAN, "'x'/['a',1]");

    assertDivide(0.5d, "true/2");
    assertDivide(Literal.NAN, "true/'y'");
    assertDivide(1d, "true/true");
    assertDivide(Literal.INFINITY, "true/null");
    assertDivide(Literal.NAN, "true/undefined");
    assertDivide(Literal.NAN, "true/{'a':1}");
    assertDivide(Literal.NAN, "true/['a',1]");

    assertDivide(0d, "null/2");
    assertDivide(Literal.NAN, "null/'y'");
    assertDivide(0d, "null/true");
    assertDivide(Literal.NAN, "null/null");
    assertDivide(Literal.NAN, "null/undefined");
    assertDivide(Literal.NAN, "null/{'a':1}");
    assertDivide(Literal.NAN, "null/['a',1]");

    assertDivide(Literal.NAN, "undefined/2");
    assertDivide(Literal.NAN, "undefined/'y'");
    assertDivide(Literal.NAN, "undefined/true");
    assertDivide(Literal.NAN, "undefined/null");
    assertDivide(Literal.NAN, "undefined/undefined");
    assertDivide(Literal.NAN, "undefined/{'a':1}");
    assertDivide(Literal.NAN, "undefined/['a',1]");

    assertDivide(Literal.NAN, "{'a':1}/2");
    assertDivide(Literal.NAN, "{'a':1}/'y'");
    assertDivide(Literal.NAN, "{'a':1}/true");
    assertDivide(Literal.NAN, "{'a':1}/null");
    assertDivide(Literal.NAN, "{'a':1}/undefined");
    assertDivide(Literal.NAN, "{'a':1}/{'a':1}");
    assertDivide(Literal.NAN, "{'a':1}/['a',1]");

    assertDivide(Literal.NAN, "['a',1]/2");
    assertDivide(Literal.NAN, "['a',1]/'y'");
    assertDivide(Literal.NAN, "['a',1]/true");
    assertDivide(Literal.NAN, "['a',1]/null");
    assertDivide(Literal.NAN, "['a',1]/undefined");
    assertDivide(Literal.NAN, "['a',1]/{'a':1}");
    assertDivide(Literal.NAN, "['a',1]/['a',1]");

  }

  private void assertDivide(Object expected, String expression) {
    ScriptVersion scriptVersion = deployScript("var result = "+expression+";");
    assertEquals(expected, startScriptExecution(scriptVersion).getVariable("result"));
  }
}

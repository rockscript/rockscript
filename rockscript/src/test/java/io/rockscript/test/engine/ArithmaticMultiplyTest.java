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

public class ArithmaticMultiplyTest extends AbstractEngineTest {

  /*
  Test was generated from (and based on) node.js code:

  multiply(1, 2);
  multiply(1, 'y');
  multiply(1, true);
  multiply(1, null);
  multiply(1, undefined);
  multiply(1, {'a':1});
  multiply(1, ['a',1]);
  console.log();

  multiply('x', 2);
  multiply('x', 'y');
  multiply('x', true);
  multiply('x', null);
  multiply('x', undefined);
  multiply('x', {'a':1});
  multiply('x', ['a',1]);
  console.log();

  multiply(true, 2);
  multiply(true, 'y');
  multiply(true, true);
  multiply(true, null);
  multiply(true, undefined);
  multiply(true, {'a':1});
  multiply(true, ['a',1]);
  console.log();

  multiply(null, 2);
  multiply(null, 'y');
  multiply(null, true);
  multiply(null, null);
  multiply(null, undefined);
  multiply(null, {'a':1});
  multiply(null, ['a',1]);
  console.log();

  multiply(undefined, 2);
  multiply(undefined, 'y');
  multiply(undefined, true);
  multiply(undefined, null);
  multiply(undefined, undefined);
  multiply(undefined, {'a':1});
  multiply(undefined, ['a',1]);
  console.log();

  multiply({'a':1}, 2);
  multiply({'a':1}, 'y');
  multiply({'a':1}, true);
  multiply({'a':1}, null);
  multiply({'a':1}, undefined);
  multiply({'a':1}, {'a':1});
  multiply({'a':1}, ['a',1]);
  console.log();

  multiply(['a',1], 2);
  multiply(['a',1], 'y');
  multiply(['a',1], true);
  multiply(['a',1], null);
  multiply(['a',1], undefined);
  multiply(['a',1], {'a':1});
  multiply(['a',1], ['a',1]);

  function multiply(l,r) {
    var result = l*r;
    console.log('    assertSubtract('+valueToString(result)+', "'+valueToString(l)+'*'+valueToString(r)+'");');
  }

  function valueToString(o) {
    return ('string'===typeof o? '\''+o+'\'' : 'object'===typeof o || 'array'===typeof o ? JSON.stringify(o).replace(new RegExp('"', 'g'), "'") :o );
  }
  */
  @Test
  public void testArithmaticMultiplications() {
    assertMultiply(2d, "1*2");
    assertMultiply(Literal.NAN, "1*'y'");
    assertMultiply(1d, "1*true");
    assertMultiply(0d, "1*null");
    assertMultiply(Literal.NAN, "1*undefined");
    assertMultiply(Literal.NAN, "1*{'a':1}");
    assertMultiply(Literal.NAN, "1*['a',1]");

    assertMultiply(Literal.NAN, "'x'*2");
    assertMultiply(Literal.NAN, "'x'*'y'");
    assertMultiply(Literal.NAN, "'x'*true");
    assertMultiply(Literal.NAN, "'x'*null");
    assertMultiply(Literal.NAN, "'x'*undefined");
    assertMultiply(Literal.NAN, "'x'*{'a':1}");
    assertMultiply(Literal.NAN, "'x'*['a',1]");

    assertMultiply(2d, "true*2");
    assertMultiply(Literal.NAN, "true*'y'");
    assertMultiply(1d, "true*true");
    assertMultiply(0d, "true*null");
    assertMultiply(Literal.NAN, "true*undefined");
    assertMultiply(Literal.NAN, "true*{'a':1}");
    assertMultiply(Literal.NAN, "true*['a',1]");

    assertMultiply(0d, "null*2");
    assertMultiply(Literal.NAN, "null*'y'");
    assertMultiply(0d, "null*true");
    assertMultiply(0d, "null*null");
    assertMultiply(Literal.NAN, "null*undefined");
    assertMultiply(Literal.NAN, "null*{'a':1}");
    assertMultiply(Literal.NAN, "null*['a',1]");

    assertMultiply(Literal.NAN, "undefined*2");
    assertMultiply(Literal.NAN, "undefined*'y'");
    assertMultiply(Literal.NAN, "undefined*true");
    assertMultiply(Literal.NAN, "undefined*null");
    assertMultiply(Literal.NAN, "undefined*undefined");
    assertMultiply(Literal.NAN, "undefined*{'a':1}");
    assertMultiply(Literal.NAN, "undefined*['a',1]");

    assertMultiply(Literal.NAN, "{'a':1}*2");
    assertMultiply(Literal.NAN, "{'a':1}*'y'");
    assertMultiply(Literal.NAN, "{'a':1}*true");
    assertMultiply(Literal.NAN, "{'a':1}*null");
    assertMultiply(Literal.NAN, "{'a':1}*undefined");
    assertMultiply(Literal.NAN, "{'a':1}*{'a':1}");
    assertMultiply(Literal.NAN, "{'a':1}*['a',1]");

    assertMultiply(Literal.NAN, "['a',1]*2");
    assertMultiply(Literal.NAN, "['a',1]*'y'");
    assertMultiply(Literal.NAN, "['a',1]*true");
    assertMultiply(Literal.NAN, "['a',1]*null");
    assertMultiply(Literal.NAN, "['a',1]*undefined");
    assertMultiply(Literal.NAN, "['a',1]*{'a':1}");
    assertMultiply(Literal.NAN, "['a',1]*['a',1]");
  }

  private void assertMultiply(Object expected, String addition) {
    ScriptVersion scriptVersion = deployScript("var result = "+addition+";");
    assertEquals(expected, startScriptExecution(scriptVersion).getVariable("result"));
  }
}

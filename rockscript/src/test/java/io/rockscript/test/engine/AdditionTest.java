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
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AdditionTest extends AbstractEngineTest {

  /*
  Test was generated from (and based on) node.js code:

  add(1, 2);
  add(1, 'y');
  add(1, true);
  add(1, null);
  add(1, undefined);
  add(1, {'a':1});
  add(1, ['a',1]);
  console.log();

  add('x', 2);
  add('x', 'y');
  add('x', true);
  add('x', null);
  add('x', undefined);
  add('x', {'a':1});
  add('x', ['a',1]);
  console.log();

  add(true, 2);
  add(true, 'y');
  add(true, true);
  add(true, null);
  add(true, undefined);
  add(true, {'a':1});
  add(true, ['a',1]);
  console.log();

  add(null, 2);
  add(null, 'y');
  add(null, true);
  add(null, null);
  add(null, undefined);
  add(null, {'a':1});
  add(null, ['a',1]);
  console.log();

  add(undefined, 2);
  add(undefined, 'y');
  add(undefined, true);
  add(undefined, null);
  add(undefined, undefined);
  add(undefined, {'a':1});
  add(undefined, ['a',1]);
  console.log();

  add({'a':1}, 2);
  add({'a':1}, 'y');
  add({'a':1}, true);
  add({'a':1}, null);
  add({'a':1}, undefined);
  add({'a':1}, {'a':1});
  add({'a':1}, ['a',1]);
  console.log();

  add(['a',1], 2);
  add(['a',1], 'y');
  add(['a',1], true);
  add(['a',1], null);
  add(['a',1], undefined);
  add(['a',1], {'a':1});
  add(['a',1], ['a',1]);

  function add(l,r) {
    var result = l+r;
    console.log('    assertAdd("'+result+'", "'+valueToString(l)+'+'+valueToString(r)+'");');
  }

  function valueToString(o) {
    return ('string'===typeof o? '\''+o+'\'' : 'object'===typeof o || 'array'===typeof o ? JSON.stringify(o).replace('"', "'") :o );
  }
  */
  @Test
  public void testLiteralAssignment() {
    assertAdd(3d, "1+2");
    assertAdd("1y", "1+'y'");
    assertAdd(2d, "1+true");
    assertAdd(1d, "1+null");
    assertAdd(Literal.NAN, "1+undefined");
    assertAdd("1[object Object]", "1+{'a':1}");
    assertAdd("1a,1", "1+['a',1]");

    assertAdd("x2", "'x'+2");
    assertAdd("xy", "'x'+'y'");
    assertAdd("xtrue", "'x'+true");
    assertAdd("xnull", "'x'+null");
    assertAdd("xundefined", "'x'+undefined");
    assertAdd("x[object Object]", "'x'+{'a':1}");
    assertAdd("xa,1", "'x'+['a',1]");

    assertAdd(3d, "true+2");
    assertAdd("truey", "true+'y'");
    assertAdd(2d, "true+true");
    assertAdd(1d, "true+null");
    assertAdd(Literal.NAN, "true+undefined");
    assertAdd("true[object Object]", "true+{'a':1}");
    assertAdd("truea,1", "true+['a',1]");

    assertAdd(2d, "null+2");
    assertAdd("nully", "null+'y'");
    assertAdd(1d, "null+true");
    assertAdd(0d, "null+null");
    assertAdd(Literal.NAN, "null+undefined");
    assertAdd("null[object Object]", "null+{'a':1}");
    assertAdd("nulla,1", "null+['a',1]");

    assertAdd(Literal.NAN, "undefined+2");
    assertAdd("undefinedy", "undefined+'y'");
    assertAdd(Literal.NAN, "undefined+true");
    assertAdd(Literal.NAN, "undefined+null");
    assertAdd(Literal.NAN, "undefined+undefined");
    assertAdd("undefined[object Object]", "undefined+{'a':1}");
    assertAdd("undefineda,1", "undefined+['a',1]");

    assertAdd("[object Object]2", "{'a':1}+2");
    assertAdd("[object Object]y", "{'a':1}+'y'");
    assertAdd("[object Object]true", "{'a':1}+true");
    assertAdd("[object Object]null", "{'a':1}+null");
    assertAdd("[object Object]undefined", "{'a':1}+undefined");
    assertAdd("[object Object][object Object]", "{'a':1}+{'a':1}");
    assertAdd("[object Object]a,1", "{'a':1}+['a',1]");

    assertAdd("a,12", "['a',1]+2");
    assertAdd("a,1y", "['a',1]+'y'");
    assertAdd("a,1true", "['a',1]+true");
    assertAdd("a,1null", "['a',1]+null");
    assertAdd("a,1undefined", "['a',1]+undefined");
    assertAdd("a,1[object Object]", "['a',1]+{'a':1}");
    assertAdd("a,1a,1", "['a',1]+['a',1]");
  }

  private void assertAdd(Object expected, String addition) {
    ScriptVersion scriptVersion = deployScript("var result = "+addition+";");
    assertEquals(expected, startScriptExecution(scriptVersion).getVariable("result"));
  }
}

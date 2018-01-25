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

import static org.junit.Assert.assertEquals;

public class AbstractCompareTest extends AbstractEngineTest {

  /*
  Node.js code used to generate this test

  compare(1, 2);
  compare(1, 'y');
  compare(1, true);
  compare(1, null);
  compare(1, undefined);
  compare(1, {'a':1});
  compare(1, ['a',1]);
  console.log();

  compare('x', 2);
  compare('x', 'y');
  compare('x', true);
  compare('x', null);
  compare('x', undefined);
  compare('x', {'a':1});
  compare('x', ['a',1]);
  console.log();

  compare(true, 2);
  compare(true, 'y');
  compare(true, true);
  compare(true, null);
  compare(true, undefined);
  compare(true, {'a':1});
  compare(true, ['a',1]);
  console.log();

  compare(null, 2);
  compare(null, 'y');
  compare(null, true);
  compare(null, null);
  compare(null, undefined);
  compare(null, {'a':1});
  compare(null, ['a',1]);
  console.log();

  compare(undefined, 2);
  compare(undefined, 'y');
  compare(undefined, true);
  compare(undefined, null);
  compare(undefined, undefined);
  compare(undefined, {'a':1});
  compare(undefined, ['a',1]);
  console.log();

  compare({'a':1}, 2);
  compare({'a':1}, 'y');
  compare({'a':1}, true);
  compare({'a':1}, null);
  compare({'a':1}, undefined);
  compare({'a':1}, {'a':1});
  compare({'a':1}, ['a',1]);
  console.log();

  compare(['a',1], 2);
  compare(['a',1], 'y');
  compare(['a',1], true);
  compare(['a',1], null);
  compare(['a',1], undefined);
  compare(['a',1], {'a':1});
  compare(['a',1], ['a',1]);
  console.log();

  function compare(l,r) {
    console.log('    assertCompare('+(l<r)+', "'+valueToString(l)+'<'+valueToString(r)+'");');
    console.log('    assertCompare('+(r<l)+', "'+valueToString(r)+'<'+valueToString(l)+'");');
  }

  function valueToString(o) {
    return ('string'===typeof o? '\''+o+'\'' : 'object'===typeof o || 'array'===typeof o ? JSON.stringify(o).replace(new RegExp('"', 'g'), "'") :o );
  }
  */

  public void assertCompare(Boolean expected, String comparison) {
    ScriptVersion scriptVersion = deployScript("var result = " + comparison + ";");
    Object result = startScriptExecution(scriptVersion).getVariable("result");
    assertEquals(expected, result);
  }

}

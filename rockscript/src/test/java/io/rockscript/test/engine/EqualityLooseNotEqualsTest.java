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
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EqualityLooseNotEqualsTest extends AbstractCompareTest {

  @Test
  public void testLooseNotEqual() {
    assertCompare(true, "1!=2");
    assertCompare(true, "2!=1");
    assertCompare(true, "1!='y'");
    assertCompare(true, "'y'!=1");
    assertCompare(false, "1!=true");
    assertCompare(false, "true!=1");
    assertCompare(true, "1!=null");
    assertCompare(true, "null!=1");
    assertCompare(true, "1!=undefined");
    assertCompare(true, "undefined!=1");
    assertCompare(true, "1!={'a':1}");
    assertCompare(true, "{'a':1}!=1");
    assertCompare(true, "1!=['a',1]");
    assertCompare(true, "['a',1]!=1");

    assertCompare(true, "'x'!=2");
    assertCompare(true, "2!='x'");
    assertCompare(true, "'x'!='y'");
    assertCompare(true, "'y'!='x'");
    assertCompare(true, "'x'!=true");
    assertCompare(true, "true!='x'");
    assertCompare(true, "'x'!=null");
    assertCompare(true, "null!='x'");
    assertCompare(true, "'x'!=undefined");
    assertCompare(true, "undefined!='x'");
    assertCompare(true, "'x'!={'a':1}");
    assertCompare(true, "{'a':1}!='x'");
    assertCompare(true, "'x'!=['a',1]");
    assertCompare(true, "['a',1]!='x'");

    assertCompare(true, "true!=2");
    assertCompare(true, "2!=true");
    assertCompare(true, "true!='y'");
    assertCompare(true, "'y'!=true");
    assertCompare(false, "true!=true");
    assertCompare(false, "true!=true");
    assertCompare(true, "true!=null");
    assertCompare(true, "null!=true");
    assertCompare(true, "true!=undefined");
    assertCompare(true, "undefined!=true");
    assertCompare(true, "true!={'a':1}");
    assertCompare(true, "{'a':1}!=true");
    assertCompare(true, "true!=['a',1]");
    assertCompare(true, "['a',1]!=true");

    assertCompare(true, "null!=2");
    assertCompare(true, "2!=null");
    assertCompare(true, "null!='y'");
    assertCompare(true, "'y'!=null");
    assertCompare(true, "null!=true");
    assertCompare(true, "true!=null");
    assertCompare(false, "null!=null");
    assertCompare(false, "null!=null");
    assertCompare(false, "null!=undefined");
    assertCompare(false, "undefined!=null");
    assertCompare(true, "null!={'a':1}");
    assertCompare(true, "{'a':1}!=null");
    assertCompare(true, "null!=['a',1]");
    assertCompare(true, "['a',1]!=null");

    assertCompare(true, "undefined!=2");
    assertCompare(true, "2!=undefined");
    assertCompare(true, "undefined!='y'");
    assertCompare(true, "'y'!=undefined");
    assertCompare(true, "undefined!=true");
    assertCompare(true, "true!=undefined");
    assertCompare(false, "undefined!=null");
    assertCompare(false, "null!=undefined");
    assertCompare(false, "undefined!=undefined");
    assertCompare(false, "undefined!=undefined");
    assertCompare(true, "undefined!={'a':1}");
    assertCompare(true, "{'a':1}!=undefined");
    assertCompare(true, "undefined!=['a',1]");
    assertCompare(true, "['a',1]!=undefined");

    assertCompare(true, "{'a':1}!=2");
    assertCompare(true, "2!={'a':1}");
    assertCompare(true, "{'a':1}!='y'");
    assertCompare(true, "'y'!={'a':1}");
    assertCompare(true, "{'a':1}!=true");
    assertCompare(true, "true!={'a':1}");
    assertCompare(true, "{'a':1}!=null");
    assertCompare(true, "null!={'a':1}");
    assertCompare(true, "{'a':1}!=undefined");
    assertCompare(true, "undefined!={'a':1}");
    assertCompare(true, "{'a':1}!={'a':1}");
    assertCompare(true, "{'a':1}!={'a':1}");
    assertCompare(true, "{'a':1}!=['a',1]");
    assertCompare(true, "['a',1]!={'a':1}");

    assertCompare(true, "['a',1]!=2");
    assertCompare(true, "2!=['a',1]");
    assertCompare(true, "['a',1]!='y'");
    assertCompare(true, "'y'!=['a',1]");
    assertCompare(true, "['a',1]!=true");
    assertCompare(true, "true!=['a',1]");
    assertCompare(true, "['a',1]!=null");
    assertCompare(true, "null!=['a',1]");
    assertCompare(true, "['a',1]!=undefined");
    assertCompare(true, "undefined!=['a',1]");
    assertCompare(true, "['a',1]!={'a':1}");
    assertCompare(true, "{'a':1}!=['a',1]");
    assertCompare(true, "['a',1]!=['a',1]");
    assertCompare(true, "['a',1]!=['a',1]");
  }
}

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

import java.io.Console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ComparatorsTest extends AbstractCompareTest {

  @Test
  public void testLessThan() {
    assertCompare(true, "1<2");
    assertCompare(false, "2<1");
    assertCompare(false, "1<'y'");
    assertCompare(false, "'y'<1");
    assertCompare(false, "1<true");
    assertCompare(false, "true<1");
    assertCompare(false, "1<null");
    assertCompare(true, "null<1");
    assertCompare(false, "1<undefined");
    assertCompare(false, "undefined<1");
    assertCompare(false, "1<{'a':1}");
    assertCompare(false, "{'a':1}<1");
    assertCompare(false, "1<['a',1]");
    assertCompare(false, "['a',1]<1");

    assertCompare(false, "'x'<2");
    assertCompare(false, "2<'x'");
    assertCompare(true, "'x'<'y'");
    assertCompare(false, "'y'<'x'");
    assertCompare(false, "'x'<true");
    assertCompare(false, "true<'x'");
    assertCompare(false, "'x'<null");
    assertCompare(false, "null<'x'");
    assertCompare(false, "'x'<undefined");
    assertCompare(false, "undefined<'x'");
    assertCompare(false, "'x'<{'a':1}");
    assertCompare(true, "{'a':1}<'x'");
    assertCompare(false, "'x'<['a',1]");
    assertCompare(true, "['a',1]<'x'");
    assertCompare(false, "['z',1]<'a'");

    assertCompare(true, "true<2");
    assertCompare(false, "2<true");
    assertCompare(false, "true<'y'");
    assertCompare(false, "'y'<true");
    assertCompare(false, "true<true");
    assertCompare(false, "true<true");
    assertCompare(false, "true<null");
    assertCompare(true, "null<true");
    assertCompare(false, "true<undefined");
    assertCompare(false, "undefined<true");
    assertCompare(false, "true<{'a':1}");
    assertCompare(false, "{'a':1}<true");
    assertCompare(false, "true<['a',1]");
    assertCompare(false, "['a',1]<true");

    assertCompare(true, "null<2");
    assertCompare(false, "2<null");
    assertCompare(false, "null<'y'");
    assertCompare(false, "'y'<null");
    assertCompare(true, "null<true");
    assertCompare(false, "true<null");
    assertCompare(false, "null<null");
    assertCompare(false, "null<null");
    assertCompare(false, "null<undefined");
    assertCompare(false, "undefined<null");
    assertCompare(false, "null<{'a':1}");
    assertCompare(false, "{'a':1}<null");
    assertCompare(false, "null<['a',1]");
    assertCompare(false, "['a',1]<null");

    assertCompare(false, "undefined<2");
    assertCompare(false, "2<undefined");
    assertCompare(false, "undefined<'y'");
    assertCompare(false, "'y'<undefined");
    assertCompare(false, "undefined<true");
    assertCompare(false, "true<undefined");
    assertCompare(false, "undefined<null");
    assertCompare(false, "null<undefined");
    assertCompare(false, "undefined<undefined");
    assertCompare(false, "undefined<undefined");
    assertCompare(false, "undefined<{'a':1}");
    assertCompare(false, "{'a':1}<undefined");
    assertCompare(false, "undefined<['a',1]");
    assertCompare(false, "['a',1]<undefined");

    assertCompare(false, "{'a':1}<2");
    assertCompare(false, "2<{'a':1}");
    assertCompare(true, "{'a':1}<'y'");
    assertCompare(false, "'y'<{'a':1}");
    assertCompare(false, "{'a':1}<true");
    assertCompare(false, "true<{'a':1}");
    assertCompare(false, "{'a':1}<null");
    assertCompare(false, "null<{'a':1}");
    assertCompare(false, "{'a':1}<undefined");
    assertCompare(false, "undefined<{'a':1}");
    assertCompare(false, "{'a':1}<{'a':1}");
    assertCompare(false, "{'a':1}<{'a':1}");
    assertCompare(true, "{'a':1}<['a',1]");
    assertCompare(false, "['a',1]<{'a':1}");

    assertCompare(false, "['a',1]<2");
    assertCompare(false, "2<['a',1]");
    assertCompare(true, "['a',1]<'y'");
    assertCompare(false, "'y'<['a',1]");
    assertCompare(false, "['a',1]<true");
    assertCompare(false, "true<['a',1]");
    assertCompare(false, "['a',1]<null");
    assertCompare(false, "null<['a',1]");
    assertCompare(false, "['a',1]<undefined");
    assertCompare(false, "undefined<['a',1]");
    assertCompare(false, "['a',1]<{'a':1}");
    assertCompare(true, "{'a':1}<['a',1]");
    assertCompare(false, "['a',1]<['a',1]");
    assertCompare(false, "['a',1]<['a',1]");
  }

  @Test
  public void testGreaterThan() {
    assertCompare(false, "1>2");
    assertCompare(true, "2>1");
    assertCompare(false, "1>'y'");
    assertCompare(false, "'y'>1");
    assertCompare(false, "1>true");
    assertCompare(false, "true>1");
    assertCompare(true, "1>null");
    assertCompare(false, "null>1");
    assertCompare(false, "1>undefined");
    assertCompare(false, "undefined>1");
    assertCompare(false, "1>{'a':1}");
    assertCompare(false, "{'a':1}>1");
    assertCompare(false, "1>['a',1]");
    assertCompare(false, "['a',1]>1");

    assertCompare(false, "'x'>2");
    assertCompare(false, "2>'x'");
    assertCompare(false, "'x'>'y'");
    assertCompare(true, "'y'>'x'");
    assertCompare(false, "'x'>true");
    assertCompare(false, "true>'x'");
    assertCompare(false, "'x'>null");
    assertCompare(false, "null>'x'");
    assertCompare(false, "'x'>undefined");
    assertCompare(false, "undefined>'x'");
    assertCompare(true, "'x'>{'a':1}");
    assertCompare(false, "{'a':1}>'x'");
    assertCompare(true, "'x'>['a',1]");
    assertCompare(false, "['a',1]>'x'");

    assertCompare(false, "true>2");
    assertCompare(true, "2>true");
    assertCompare(false, "true>'y'");
    assertCompare(false, "'y'>true");
    assertCompare(false, "true>true");
    assertCompare(false, "true>true");
    assertCompare(true, "true>null");
    assertCompare(false, "null>true");
    assertCompare(false, "true>undefined");
    assertCompare(false, "undefined>true");
    assertCompare(false, "true>{'a':1}");
    assertCompare(false, "{'a':1}>true");
    assertCompare(false, "true>['a',1]");
    assertCompare(false, "['a',1]>true");

    assertCompare(false, "null>2");
    assertCompare(true, "2>null");
    assertCompare(false, "null>'y'");
    assertCompare(false, "'y'>null");
    assertCompare(false, "null>true");
    assertCompare(true, "true>null");
    assertCompare(false, "null>null");
    assertCompare(false, "null>null");
    assertCompare(false, "null>undefined");
    assertCompare(false, "undefined>null");
    assertCompare(false, "null>{'a':1}");
    assertCompare(false, "{'a':1}>null");
    assertCompare(false, "null>['a',1]");
    assertCompare(false, "['a',1]>null");

    assertCompare(false, "undefined>2");
    assertCompare(false, "2>undefined");
    assertCompare(false, "undefined>'y'");
    assertCompare(false, "'y'>undefined");
    assertCompare(false, "undefined>true");
    assertCompare(false, "true>undefined");
    assertCompare(false, "undefined>null");
    assertCompare(false, "null>undefined");
    assertCompare(false, "undefined>undefined");
    assertCompare(false, "undefined>undefined");
    assertCompare(false, "undefined>{'a':1}");
    assertCompare(false, "{'a':1}>undefined");
    assertCompare(false, "undefined>['a',1]");
    assertCompare(false, "['a',1]>undefined");

    assertCompare(false, "{'a':1}>2");
    assertCompare(false, "2>{'a':1}");
    assertCompare(false, "{'a':1}>'y'");
    assertCompare(true, "'y'>{'a':1}");
    assertCompare(false, "{'a':1}>true");
    assertCompare(false, "true>{'a':1}");
    assertCompare(false, "{'a':1}>null");
    assertCompare(false, "null>{'a':1}");
    assertCompare(false, "{'a':1}>undefined");
    assertCompare(false, "undefined>{'a':1}");
    assertCompare(false, "{'a':1}>{'a':1}");
    assertCompare(false, "{'a':1}>{'a':1}");
    assertCompare(false, "{'a':1}>['a',1]");
    assertCompare(true, "['a',1]>{'a':1}");

    assertCompare(false, "['a',1]>2");
    assertCompare(false, "2>['a',1]");
    assertCompare(false, "['a',1]>'y'");
    assertCompare(true, "'y'>['a',1]");
    assertCompare(false, "['a',1]>true");
    assertCompare(false, "true>['a',1]");
    assertCompare(false, "['a',1]>null");
    assertCompare(false, "null>['a',1]");
    assertCompare(false, "['a',1]>undefined");
    assertCompare(false, "undefined>['a',1]");
    assertCompare(true, "['a',1]>{'a':1}");
    assertCompare(false, "{'a':1}>['a',1]");
    assertCompare(false, "['a',1]>['a',1]");
    assertCompare(false, "['a',1]>['a',1]");
  }

  @Test
  public void testLessThanOrEqual() {
    assertCompare(true, "1<=2");
    assertCompare(false, "2<=1");
    assertCompare(false, "1<='y'");
    assertCompare(false, "'y'<=1");
    assertCompare(true, "1<=true");
    assertCompare(true, "true<=1");
    assertCompare(false, "1<=null");
    assertCompare(true, "null<=1");
    assertCompare(false, "1<=undefined");
    assertCompare(false, "undefined<=1");
    assertCompare(false, "1<={'a':1}");
    assertCompare(false, "{'a':1}<=1");
    assertCompare(false, "1<=['a',1]");
    assertCompare(false, "['a',1]<=1");

    assertCompare(false, "'x'<=2");
    assertCompare(false, "2<='x'");
    assertCompare(true, "'x'<='y'");
    assertCompare(false, "'y'<='x'");
    assertCompare(false, "'x'<=true");
    assertCompare(false, "true<='x'");
    assertCompare(false, "'x'<=null");
    assertCompare(false, "null<='x'");
    assertCompare(false, "'x'<=undefined");
    assertCompare(false, "undefined<='x'");
    assertCompare(false, "'x'<={'a':1}");
    assertCompare(true, "{'a':1}<='x'");
    assertCompare(false, "'x'<=['a',1]");
    assertCompare(true, "['a',1]<='x'");

    assertCompare(true, "true<=2");
    assertCompare(false, "2<=true");
    assertCompare(false, "true<='y'");
    assertCompare(false, "'y'<=true");
    assertCompare(true, "true<=true");
    assertCompare(true, "true<=true");
    assertCompare(false, "true<=null");
    assertCompare(true, "null<=true");
    assertCompare(false, "true<=undefined");
    assertCompare(false, "undefined<=true");
    assertCompare(false, "true<={'a':1}");
    assertCompare(false, "{'a':1}<=true");
    assertCompare(false, "true<=['a',1]");
    assertCompare(false, "['a',1]<=true");

    assertCompare(true, "null<=2");
    assertCompare(false, "2<=null");
    assertCompare(false, "null<='y'");
    assertCompare(false, "'y'<=null");
    assertCompare(true, "null<=true");
    assertCompare(false, "true<=null");
    assertCompare(true, "null<=null");
    assertCompare(true, "null<=null");
    assertCompare(false, "null<=undefined");
    assertCompare(false, "undefined<=null");
    assertCompare(false, "null<={'a':1}");
    assertCompare(false, "{'a':1}<=null");
    assertCompare(false, "null<=['a',1]");
    assertCompare(false, "['a',1]<=null");

    assertCompare(false, "undefined<=2");
    assertCompare(false, "2<=undefined");
    assertCompare(false, "undefined<='y'");
    assertCompare(false, "'y'<=undefined");
    assertCompare(false, "undefined<=true");
    assertCompare(false, "true<=undefined");
    assertCompare(false, "undefined<=null");
    assertCompare(false, "null<=undefined");
    assertCompare(false, "undefined<=undefined");
    assertCompare(false, "undefined<=undefined");
    assertCompare(false, "undefined<={'a':1}");
    assertCompare(false, "{'a':1}<=undefined");
    assertCompare(false, "undefined<=['a',1]");
    assertCompare(false, "['a',1]<=undefined");

    assertCompare(false, "{'a':1}<=2");
    assertCompare(false, "2<={'a':1}");
    assertCompare(true, "{'a':1}<='y'");
    assertCompare(false, "'y'<={'a':1}");
    assertCompare(false, "{'a':1}<=true");
    assertCompare(false, "true<={'a':1}");
    assertCompare(false, "{'a':1}<=null");
    assertCompare(false, "null<={'a':1}");
    assertCompare(false, "{'a':1}<=undefined");
    assertCompare(false, "undefined<={'a':1}");
    assertCompare(true, "{'a':1}<={'a':1}");
    assertCompare(true, "{'a':1}<={'a':1}");
    assertCompare(true, "{'a':1}<=['a',1]");
    assertCompare(false, "['a',1]<={'a':1}");

    assertCompare(false, "['a',1]<=2");
    assertCompare(false, "2<=['a',1]");
    assertCompare(true, "['a',1]<='y'");
    assertCompare(false, "'y'<=['a',1]");
    assertCompare(false, "['a',1]<=true");
    assertCompare(false, "true<=['a',1]");
    assertCompare(false, "['a',1]<=null");
    assertCompare(false, "null<=['a',1]");
    assertCompare(false, "['a',1]<=undefined");
    assertCompare(false, "undefined<=['a',1]");
    assertCompare(false, "['a',1]<={'a':1}");
    assertCompare(true, "{'a':1}<=['a',1]");
    assertCompare(true, "['a',1]<=['a',1]");
    assertCompare(true, "['a',1]<=['a',1]");
  }

  @Test
  public void testGreaterThanOrEqual() {
    assertCompare(false, "1>=2");
    assertCompare(true, "2>=1");
    assertCompare(false, "1>='y'");
    assertCompare(false, "'y'>=1");
    assertCompare(true, "1>=true");
    assertCompare(true, "true>=1");
    assertCompare(true, "1>=null");
    assertCompare(false, "null>=1");
    assertCompare(false, "1>=undefined");
    assertCompare(false, "undefined>=1");
    assertCompare(false, "1>={'a':1}");
    assertCompare(false, "{'a':1}>=1");
    assertCompare(false, "1>=['a',1]");
    assertCompare(false, "['a',1]>=1");

    assertCompare(false, "'x'>=2");
    assertCompare(false, "2>='x'");
    assertCompare(false, "'x'>='y'");
    assertCompare(true, "'y'>='x'");
    assertCompare(false, "'x'>=true");
    assertCompare(false, "true>='x'");
    assertCompare(false, "'x'>=null");
    assertCompare(false, "null>='x'");
    assertCompare(false, "'x'>=undefined");
    assertCompare(false, "undefined>='x'");
    assertCompare(true, "'x'>={'a':1}");
    assertCompare(false, "{'a':1}>='x'");
    assertCompare(true, "'x'>=['a',1]");
    assertCompare(false, "['a',1]>='x'");

    assertCompare(false, "true>=2");
    assertCompare(true, "2>=true");
    assertCompare(false, "true>='y'");
    assertCompare(false, "'y'>=true");
    assertCompare(true, "true>=true");
    assertCompare(true, "true>=true");
    assertCompare(true, "true>=null");
    assertCompare(false, "null>=true");
    assertCompare(false, "true>=undefined");
    assertCompare(false, "undefined>=true");
    assertCompare(false, "true>={'a':1}");
    assertCompare(false, "{'a':1}>=true");
    assertCompare(false, "true>=['a',1]");
    assertCompare(false, "['a',1]>=true");

    assertCompare(false, "null>=2");
    assertCompare(true, "2>=null");
    assertCompare(false, "null>='y'");
    assertCompare(false, "'y'>=null");
    assertCompare(false, "null>=true");
    assertCompare(true, "true>=null");
    assertCompare(true, "null>=null");
    assertCompare(true, "null>=null");
    assertCompare(false, "null>=undefined");
    assertCompare(false, "undefined>=null");
    assertCompare(false, "null>={'a':1}");
    assertCompare(false, "{'a':1}>=null");
    assertCompare(false, "null>=['a',1]");
    assertCompare(false, "['a',1]>=null");

    assertCompare(false, "undefined>=2");
    assertCompare(false, "2>=undefined");
    assertCompare(false, "undefined>='y'");
    assertCompare(false, "'y'>=undefined");
    assertCompare(false, "undefined>=true");
    assertCompare(false, "true>=undefined");
    assertCompare(false, "undefined>=null");
    assertCompare(false, "null>=undefined");
    assertCompare(false, "undefined>=undefined");
    assertCompare(false, "undefined>=undefined");
    assertCompare(false, "undefined>={'a':1}");
    assertCompare(false, "{'a':1}>=undefined");
    assertCompare(false, "undefined>=['a',1]");
    assertCompare(false, "['a',1]>=undefined");

    assertCompare(false, "{'a':1}>=2");
    assertCompare(false, "2>={'a':1}");
    assertCompare(false, "{'a':1}>='y'");
    assertCompare(true, "'y'>={'a':1}");
    assertCompare(false, "{'a':1}>=true");
    assertCompare(false, "true>={'a':1}");
    assertCompare(false, "{'a':1}>=null");
    assertCompare(false, "null>={'a':1}");
    assertCompare(false, "{'a':1}>=undefined");
    assertCompare(false, "undefined>={'a':1}");
    assertCompare(true, "{'a':1}>={'a':1}");
    assertCompare(true, "{'a':1}>={'a':1}");
    assertCompare(false, "{'a':1}>=['a',1]");
    assertCompare(true, "['a',1]>={'a':1}");

    assertCompare(false, "['a',1]>=2");
    assertCompare(false, "2>=['a',1]");
    assertCompare(false, "['a',1]>='y'");
    assertCompare(true, "'y'>=['a',1]");
    assertCompare(false, "['a',1]>=true");
    assertCompare(false, "true>=['a',1]");
    assertCompare(false, "['a',1]>=null");
    assertCompare(false, "null>=['a',1]");
    assertCompare(false, "['a',1]>=undefined");
    assertCompare(false, "undefined>=['a',1]");
    assertCompare(true, "['a',1]>={'a':1}");
    assertCompare(false, "{'a':1}>=['a',1]");
    assertCompare(true, "['a',1]>=['a',1]");
    assertCompare(true, "['a',1]>=['a',1]");
  }

}

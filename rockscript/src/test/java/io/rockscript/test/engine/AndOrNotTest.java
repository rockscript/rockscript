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

public class AndOrNotTest extends AbstractEngineTest {

  @Test
  public void testAndLogicalOperator() {
    assertConditionTrue("true && true");
    assertConditionFalse("true && false");
    assertConditionFalse("false && true");
    assertConditionFalse("false && false");
  }

  @Test
  public void testOrLogicalOperator() {
    assertConditionTrue("true || true");
    assertConditionTrue("true || false");
    assertConditionTrue("false || true");
    assertConditionFalse("false || false");
  }

  @Test
  public void testNotLogicalOperator() {
    assertConditionFalse("!true");
    assertConditionTrue("!false");
  }

  @Test
  public void testComplexLogicalOperator() {
    assertConditionFalse("!false && false");
    assertConditionTrue("true || false && false");
    assertConditionFalse("(true || false) && false");
  }

  private void assertConditionTrue(String condition) {
    assertCondition(condition, true);
  }
  private void assertConditionFalse(String condition) {
    assertCondition(condition, false);
  }
  private void assertCondition(String condition, boolean expected) {
    ScriptVersion scriptVersion = deployScript("var result = "+condition+";");
    assertEquals(expected, startScriptExecution(scriptVersion).getVariable("result"));
  }
}

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

import io.rockscript.Configuration;
import io.rockscript.Engine;
import io.rockscript.api.events.ExecutionEvent;
import io.rockscript.api.model.ScriptExecution;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.LockOperationEnd;
import io.rockscript.service.ServiceFunctionInput;
import io.rockscript.service.ServiceFunctionOutput;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class IfThenElseTest extends AbstractEngineTest {

  @Test
  public void testIfThenElse() {
    ScriptVersion scriptVersion = deployScript(
      "var result; \n" +
      "if (system.input) { \n" +
      "  result = 'affirmative'; \n" +
      "} else { \n" +
      "  result = 'negative'; \n" +
      "}");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion, true);
    assertEquals("affirmative", scriptExecution.getVariable("result"));

    scriptExecution = startScriptExecution(scriptVersion, false);
    assertEquals("negative", scriptExecution.getVariable("result"));
  }

}

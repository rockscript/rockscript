/*
 * Copyright (c) 2017 RockScript.io.
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

import io.rockscript.test.TestEngine;
import io.rockscript.service.ServiceFunctionInput;
import io.rockscript.service.ServiceFunctionOutput;
import io.rockscript.api.commands.EndServiceFunctionCommand;
import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.test.ScriptExecutionComparator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;
import static org.junit.Assert.assertEquals;

public class SerializationTest extends AbstractEngineTest {

  protected static Logger log = LoggerFactory.getLogger(SerializationTest.class);

  List<ServiceFunctionInput> serviceFunctionInputs = new ArrayList<>();

  @Override
  protected TestEngine initializeEngine() {
    // This ensures that each test will get a new CommandExecutorService
    // so that the tests can customize the import resolver without
    // polluting any cached script services.
    return new TestEngine().start();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSerialization() {
    engine.getImportResolver().createImport("helloService")
      .put("hi", input -> {
        return ServiceFunctionOutput.endFunction(input.getArg(0) + " world");
      })
      .put("world", input -> {
        serviceFunctionInputs.add(input);
        return ServiceFunctionOutput.waitForFunctionEndCallback();
      });

    ScriptVersion scriptVersion = deployScript(
        "var helloService = system.import('helloService'); \n" +
            "var response = helloService.hi(system.input.message); \n" +
            "helloService.world(response);");

    EngineScriptExecution engineScriptExecution = new StartScriptExecutionCommand()
        .scriptVersionId(scriptVersion.getId())
        .input(hashMap(
            entry("message", "hello")
        ))
        .execute(engine)
        .getEngineScriptExecution();

    String scriptExecutionId = engineScriptExecution.getId();

    new ScriptExecutionComparator()
      .assertEquals(engineScriptExecution, reloadScriptExecution(scriptExecutionId));

    ServiceFunctionInput input = serviceFunctionInputs.get(0);

    assertEquals("hello world", input.getArg(0));

    engineScriptExecution = new EndServiceFunctionCommand()
          .continuationReference(input.getContinuationReference())
          .result(null)
          .execute(engine)
          .getEngineScriptExecution();

    new ScriptExecutionComparator()
        .assertEquals(engineScriptExecution, reloadScriptExecution(scriptExecutionId));
  }

  private EngineScriptExecution reloadScriptExecution(String scriptExecutionId) {
    return engine
        .getEventStore()
        .findScriptExecutionById(scriptExecutionId);
  }
}

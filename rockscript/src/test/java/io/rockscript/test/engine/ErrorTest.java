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

import io.rockscript.Engine;
import io.rockscript.TestEngine;
import io.rockscript.api.model.ScriptExecution;
import io.rockscript.api.model.ScriptVersion;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorTest extends AbstractEngineTest {

  protected static Logger log = LoggerFactory.getLogger(ErrorTest.class);

  @Override
  protected Engine initializeEngine() {
    // This ensures that each test will get a new CommandExecutorService
    // so that the tests can customize the import resolver without
    // polluting any cached script services.
    return new TestEngine().start();
  }

  @Test
  public void testAsynchronousActivity() {
    engine.getImportResolver().createImport("problematicService")
      .put("buzzz", input -> {
        throw new RuntimeException("buzzz");
      });

    ScriptVersion scriptVersion = deployScript(
      "var problematicService = system.import('problematicService'); \n" +
      "problematicService.buzzz(); ");

    ScriptExecution scriptExecution = startScriptExecution(scriptVersion);

    engine.getEventStore().getEvents().forEach(e-> log.debug(e.toString()));
  }
}

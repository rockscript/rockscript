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

import com.google.gson.Gson;
import io.rockscript.TestEngine;
import io.rockscript.api.commands.DeployScriptVersionCommand;
import io.rockscript.api.commands.EndServiceFunctionCommand;
import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.api.model.ScriptExecution;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.ContinuationReference;
import io.rockscript.engine.impl.Time;
import io.rockscript.http.servlet.ServerRequest;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AbstractEngineTest {

  protected static Logger log = LoggerFactory.getLogger(AbstractEngineTest.class);

  protected static Map<Class<? extends EngineProvider>,TestEngine> cachedEngines = new HashMap<>();

  protected TestEngine engine;
  protected Gson gson;

  @Before
  public void setUp() {
    engine = initializeEngine();
    gson = engine.getGson();
    resetNow();
  }

  public <T> T parseBodyAs(ServerRequest request, Type type) {
    return gson.fromJson(request.getBodyAsString(), type);
  }

  @SuppressWarnings("deprecation")
  @After
  public void tearDown() {
    engine
      .getHttpClient()
      .getApacheHttpClient()
      .getConnectionManager()
      .closeIdleConnections(0, TimeUnit.NANOSECONDS);
  }

  /** Override this method if you want your CommandExecutorService to be
   * created for each test.
   *
   * Overwrite {@link #getEngineProvider()} if you want to
   * customize and cache a CommandExecutorService in your tests. */
  protected TestEngine initializeEngine() {
    EngineProvider engineProvider = getEngineProvider();
    Class<? extends EngineProvider> providerClass = engineProvider.getClass();
    TestEngine engine = cachedEngines.get(providerClass);
    if (engine==null) {
      engine = engineProvider.createEngine();
      cachedEngines.put(providerClass, engine);
    }
    return engine;
  }

  protected interface EngineProvider {
    TestEngine createEngine();
  }

  /** Override this method to use a customized Engine in your tests.
   * The AbstractEngineTest will cache the created engine between all tests
   * that use the same engine provider.
   *
   * Overwrite {@link #initializeEngine()} if you want your Engine
   * to be created for each test. */
  protected EngineProvider getEngineProvider() {
    return new EngineProvider() {
      @Override
      public TestEngine createEngine() {
        return new TestEngine().start();
      }
    };
  }

  static class TestTime extends Time {
    static void setNow(Instant now) {
      Time.now = now;
    }
  }

  public void setNow(Instant now) {
    TestTime.setNow(now);
  }

  public void resetNow() {
    TestTime.setNow(null);
  }


  public ScriptVersion deployScript(String scriptText) {
    return new DeployScriptVersionCommand()
        .scriptText(scriptText)
        .execute(engine)
        .throwIfErrors();
  }

  public ScriptExecution startScriptExecution(ScriptVersion scriptVersion) {
    return startScriptExecution(scriptVersion.getId(), null);
  }

  public ScriptExecution startScriptExecution(ScriptVersion scriptVersion, Object input) {
    return startScriptExecution(scriptVersion.getId(), input);
  }

  public ScriptExecution startScriptExecution(String scriptVersionId) {
    return startScriptExecution(scriptVersionId, null);
  }

  public ScriptExecution startScriptExecution(String scriptVersionId, Object input) {
    return new StartScriptExecutionCommand()
        .scriptVersionId(scriptVersionId)
        .input(input)
        .execute(engine)
        .getScriptExecution();
  }

  public ScriptExecution endFunction(ContinuationReference continuationReference) {
    return endFunction(continuationReference, null);
  }

  public ScriptExecution endFunction(ContinuationReference continuationReference, Object result) {
    return new EndServiceFunctionCommand()
        .continuationReference(continuationReference)
        .result(result)
        .execute(engine)
        .getScriptExecution();
  }

}

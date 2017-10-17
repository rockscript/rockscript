/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript.test;

import io.rockscript.engine.*;
import io.rockscript.engine.impl.ContinuationReference;
import io.rockscript.request.RequestExecutorService;
import io.rockscript.request.RequestExecutorServiceImpl;
import io.rockscript.request.command.DeployScriptCommand;
import io.rockscript.request.command.EndActivityCommand;
import io.rockscript.request.command.EngineStartScriptExecutionResponse;
import io.rockscript.request.command.StartScriptExecutionCommand;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScriptTest {

  protected static Logger log = LoggerFactory.getLogger(ScriptTest.class);

  protected static Map<Class<? extends ScriptServiceProvider>,RequestExecutorService> scriptServiceCache = new HashMap<>();

  protected RequestExecutorService requestExecutorService;

  @Before
  public void setUp() {
    requestExecutorService = initializeScriptService();
  }

  @SuppressWarnings("deprecation")
  @After
  public void tearDown() {
    ((RequestExecutorServiceImpl) requestExecutorService).getConfiguration()
      .getHttp()
      .getApacheHttpClient()
      .getConnectionManager()
      .closeIdleConnections(0, TimeUnit.NANOSECONDS);
  }

  /** Override this method if you want your RequestExecutorService to be
   * created for each test.
   *
   * Overwrite {@link #getScriptServiceProvider()} if you want to
   * customize and cache a RequestExecutorService in your tests. */
  protected RequestExecutorService initializeScriptService() {
    ScriptServiceProvider scriptServiceProvider = getScriptServiceProvider();
    Class<? extends ScriptServiceProvider> providerClass = scriptServiceProvider.getClass();
    RequestExecutorService requestExecutorService = scriptServiceCache.get(providerClass);
    if (requestExecutorService==null) {
      requestExecutorService = scriptServiceProvider.createScriptService();
      scriptServiceCache.put(providerClass, requestExecutorService);
    }
    return requestExecutorService;
  }

  protected interface ScriptServiceProvider {
    RequestExecutorService createScriptService();
  }

  /** Override this method to customize and cache a RequestExecutorService in your tests.
   *
   * Overwrite {@link #initializeScriptService()} if you want your RequestExecutorService
   * to be created for each test. */
  protected ScriptServiceProvider getScriptServiceProvider() {
    return new ScriptServiceProvider() {
      @Override
      public RequestExecutorService createScriptService() {
        return new TestConfiguration().build();
      }
    };
  }

  public Script deployScript(String scriptText) {
    return requestExecutorService.execute(new DeployScriptCommand()
        .scriptText(scriptText))
      .throwIfErrors();
  }

  public ScriptExecution startScriptExecution(Script script) {
    return startScriptExecution(script.getId(), null);
  }

  public ScriptExecution startScriptExecution(Script script, Object input) {
    return startScriptExecution(script.getId(), input);
  }

  public ScriptExecution startScriptExecution(String scriptId) {
    return startScriptExecution(scriptId, null);
  }

  public ScriptExecution startScriptExecution(String scriptId, Object input) {
    EngineStartScriptExecutionResponse response = requestExecutorService.execute(new StartScriptExecutionCommand()
        .scriptId(scriptId)
        .input(input));
    return response.getScriptExecution();
  }

  public ScriptExecution endActivity(ContinuationReference continuationReference) {
    return endActivity(continuationReference, null);
  }

  public ScriptExecution endActivity(ContinuationReference continuationReference, Object result) {
    return requestExecutorService.execute(new EndActivityCommand()
        .continuationReference(continuationReference)
        .result(result))
      .getScriptExecution();
  }

  protected Configuration getConfiguration() {
    return ((RequestExecutorServiceImpl) requestExecutorService).getConfiguration();
  }

  public static void assertContains(String expectedSubstring, String text) {
    if (text==null || !text.contains(expectedSubstring)) {
      throw new AssertionError("Expected substring '"+expectedSubstring+"', but was '"+text+"'");
    }
  }
}

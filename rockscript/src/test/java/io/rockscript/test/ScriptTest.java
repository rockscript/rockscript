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

import io.rockscript.api.Command;
import io.rockscript.api.CommandExecutorService;
import io.rockscript.api.CommandExecutorServiceImpl;
import io.rockscript.api.Response;
import io.rockscript.api.commands.DeployScriptCommand;
import io.rockscript.api.commands.EndActivityCommand;
import io.rockscript.api.commands.EngineStartScriptExecutionResponse;
import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.engine.Configuration;
import io.rockscript.engine.Script;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.engine.TestConfiguration;
import io.rockscript.engine.impl.ContinuationReference;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScriptTest {

  protected static Logger log = LoggerFactory.getLogger(ScriptTest.class);

  protected static Map<Class<? extends ScriptServiceProvider>,CommandExecutorService> scriptServiceCache = new HashMap<>();

  protected CommandExecutorService commandExecutorService;

  @Before
  public void setUp() {
    commandExecutorService = initializeScriptService();
  }

  @SuppressWarnings("deprecation")
  @After
  public void tearDown() {
    ((CommandExecutorServiceImpl) commandExecutorService).getConfiguration()
      .getHttp()
      .getApacheHttpClient()
      .getConnectionManager()
      .closeIdleConnections(0, TimeUnit.NANOSECONDS);
  }

  /** Override this method if you want your CommandExecutorService to be
   * created for each test.
   *
   * Overwrite {@link #getScriptServiceProvider()} if you want to
   * customize and cache a CommandExecutorService in your tests. */
  protected CommandExecutorService initializeScriptService() {
    ScriptServiceProvider scriptServiceProvider = getScriptServiceProvider();
    Class<? extends ScriptServiceProvider> providerClass = scriptServiceProvider.getClass();
    CommandExecutorService commandExecutorService = scriptServiceCache.get(providerClass);
    if (commandExecutorService==null) {
      commandExecutorService = scriptServiceProvider.createScriptService();
      scriptServiceCache.put(providerClass, commandExecutorService);
    }
    return commandExecutorService;
  }

  protected interface ScriptServiceProvider {
    CommandExecutorService createScriptService();
  }

  /** Override this method to customize and cache a CommandExecutorService in your tests.
   *
   * Overwrite {@link #initializeScriptService()} if you want your CommandExecutorService
   * to be created for each test. */
  protected ScriptServiceProvider getScriptServiceProvider() {
    return new ScriptServiceProvider() {
      @Override
      public CommandExecutorService createScriptService() {
        return new TestConfiguration().build();
      }
    };
  }

  public <R extends Response> R execute(Command<R> command) {
    return commandExecutorService.execute(command);
  }

  public Script deployScript(String scriptText) {
    return execute(new DeployScriptCommand()
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
    EngineStartScriptExecutionResponse response = commandExecutorService.execute(new StartScriptExecutionCommand()
        .scriptId(scriptId)
        .input(input));
    return response.getScriptExecution();
  }

  public ScriptExecution endActivity(ContinuationReference continuationReference) {
    return endActivity(continuationReference, null);
  }

  public ScriptExecution endActivity(ContinuationReference continuationReference, Object result) {
    return commandExecutorService.execute(new EndActivityCommand()
        .continuationReference(continuationReference)
        .result(result))
      .getScriptExecution();
  }

  protected Configuration getConfiguration() {
    return ((CommandExecutorServiceImpl) commandExecutorService).getConfiguration();
  }

  public static void assertContains(String expectedSubstring, String text) {
    if (text==null || !text.contains(expectedSubstring)) {
      throw new AssertionError("Expected substring '"+expectedSubstring+"', but was '"+text+"'");
    }
  }
}

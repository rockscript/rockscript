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

import io.rockscript.ScriptService;
import io.rockscript.TestConfiguration;
import io.rockscript.engine.Script;

import java.util.HashMap;
import java.util.Map;

public class ScriptTest {

  private static Map<Class<? extends ScriptServiceProvider>,ScriptService> scriptServiceCache = new HashMap<>();
  private ScriptServiceProvider scriptServiceProvider = getScriptServiceProvider();

  protected interface ScriptServiceProvider {
    ScriptService createScriptService();
  }

  protected ScriptService scriptService = initializeScriptService();

  /** Override this method to customize and cache a ScriptService in your tests.
   *
   * Overwrite {@link #initializeScriptService()} if you want your ScriptService
   * to be created for each test. */
  protected ScriptServiceProvider getScriptServiceProvider() {
    return new ScriptServiceProvider() {
      @Override
      public ScriptService createScriptService() {
        return new TestConfiguration().build();
      }
    };
  }

  /** Override this method if you want your ScriptService to be
   * created for each test.
   *
   * Overwrite {@link #getScriptServiceProvider()} if you want to
   * customize and cache a ScriptService in your tests. */
  protected ScriptService initializeScriptService() {
    Class<? extends ScriptServiceProvider> providerClass = scriptServiceProvider.getClass();
    ScriptService scriptService = scriptServiceCache.get(providerClass);
    if (scriptService==null) {
      scriptService = scriptServiceProvider.createScriptService();
      scriptServiceCache.put(providerClass, scriptService);
    }
    return scriptService;
  }

  public Script deploy(String scriptText) {
    return scriptService
      .newDeployScriptCommand()
      .text(scriptText)
      .execute();
  }
}

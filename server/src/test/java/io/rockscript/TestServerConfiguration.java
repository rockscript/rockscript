/*
 * Copyright ©2017, RockScript.io. All rights reserved.
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
package io.rockscript;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import io.rockscript.action.ActionOutput;
import io.rockscript.engine.JsonObject;
import io.rockscript.http.test.ServerExceptionInterceptor;
import io.rockscript.test.TestEngine;

public class TestServerConfiguration extends ServerConfiguration {

  TestService testService;

  public TestServerConfiguration(TestService testService) {
    this.testService = testService;
    asyncHttpServerConfiguration
      .interceptor(new ServerExceptionInterceptor())
      .services(Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Engine.class).toInstance(createTestEngine());
        }
      }));
  }

  private TestEngine createTestEngine() {
    TestEngine engine = new TestEngine();
    engine.getServiceLocator()
      .getImportResolver()
      .add("rockscript.io/test-service", new JsonObject()
        .put("doLongRunning", input -> {
            testService.add(input);
            return ActionOutput.waitForFunctionToCompleteAsync();
          }
        )
      );
    return engine;
  }
}

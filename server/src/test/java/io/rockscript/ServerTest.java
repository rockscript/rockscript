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

import java.util.List;

import com.google.gson.reflect.TypeToken;
import io.rockscript.action.ActionOutput;
import io.rockscript.command.DeployScriptCommand;
import io.rockscript.command.StartScriptCommand;
import io.rockscript.engine.EventJson;
import io.rockscript.engine.JsonObject;
import io.rockscript.http.test.AbstractServerTest;
import io.rockscript.netty.router.AsyncHttpServer;
import io.rockscript.test.TestEngine;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class ServerTest extends AbstractServerTest {

  static TestService testService;
  static Server server;

  @Before
  public void setUp() {
    super.setUp();
    if (server==null) {
      TestEngine testEngine = createTestEngine();
      testService = new TestService(testEngine);
      server = new DevServer(testEngine, testService);
      server.startup();
    } else {
      testService.reset();
    }
  }

  static TestEngine createTestEngine() {
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

  @AfterClass
  public static void tearDownStatic() {
    server.shutdown();
    server.waitForShutdown();
  }

  @Test
  public void testServer() {
    DeployScriptCommand.ResponseJson deployScriptResponse = POST("command")
      .bodyJson(new DeployScriptCommand()
         .script(
           "var t = system.import('rockscript.io/test-service'); "+
           "t.doLongRunning('hello'); "))
      .execute()
      .assertStatusOk()
      .body(DeployScriptCommand.ResponseJson.class);

    String scriptId = deployScriptResponse.scriptId;

    assertEquals("1", scriptId);

    StartScriptCommand.ResponseJson startScriptResponse = POST("command")
      .bodyJson(new StartScriptCommand()
                .scriptId(scriptId))
      .execute()
      .assertStatusOk()
      .body(StartScriptCommand.ResponseJson.class);

    String scriptExecutionId = startScriptResponse.scriptExecutionId;

    assertEquals(1, testService.inputs.size());

    testService.endAction(0, "by");
  }

  @Test
  public void testEvents() {
    DeployScriptCommand.ResponseJson deployScriptResponse = POST("command")
      .bodyJson(new DeployScriptCommand()
        .script(
          "var t = system.import('rockscript.io/test-service'); "+
          "t.doLongRunning('hello'); "))
      .execute()
      .assertStatusOk()
      .body(DeployScriptCommand.ResponseJson.class);

    String scriptId = deployScriptResponse.scriptId;

    StartScriptCommand.ResponseJson startScriptResponse = POST("command")
      .bodyJson(new StartScriptCommand()
                  .scriptId(scriptId))
      .execute()
      .assertStatusOk()
      .body(StartScriptCommand.ResponseJson.class);

    List<EventJson> eventJsons = GET("events")
      .execute()
      .assertStatusOk()
      .body(new TypeToken<List<EventJson>>(){}.getType());

    assertEquals(10, eventJsons.size());
  }

  @Override
  public AsyncHttpServer getNettyServer() {
    return server.asyncHttpServer;
  }
}

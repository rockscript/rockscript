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

import com.google.gson.reflect.TypeToken;
import io.rockscript.engine.Event;
import io.rockscript.http.test.AbstractServerTest;
import io.rockscript.netty.router.AsyncHttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ServerTest extends AbstractServerTest {

  static Server server;

  @BeforeClass
  public static void setUpStatic() {
    if (server==null) {
      server = new DevServer();
      server.startup();
    }
  }

  @AfterClass
  public static void tearDownStatic() {
    server.shutdown();
    server.waitForShutdown();
  }

  @Test
  public void testServer() {
    DeployScriptResponse deployScriptResponse = POST("command")
      .bodyJson(new DeployScriptCommand()
          .scriptText(""))
      .execute()
      .assertStatusOk()
      .body(DeployScriptResponse.class);

    String scriptId = deployScriptResponse.getId();

    assertNotNull(scriptId);

    StartScriptExecutionResponse startScriptResponse = POST("command")
      .bodyJson(new StartScriptExecutionCommand()
          .scriptId(scriptId))
      .execute()
      .assertStatusOk()
      .body(StartScriptExecutionResponse.class);

    String scriptExecutionId = startScriptResponse.getScriptExecutionId();
  }

  @Test
  public void testEvents() {
    DeployScriptResponse deployScriptResponse = POST("command")
      .bodyJson(new DeployScriptCommand()
        .scriptText("var msg = {hello: 'world'};"))
      .execute()
      .assertStatusOk()
      .body(DeployScriptResponse.class);

    String scriptId = deployScriptResponse.getId();

    StartScriptExecutionResponse startScriptResponse = POST("command")
      .bodyJson(new StartScriptExecutionCommand()
        .scriptId(scriptId))
      .execute()
      .assertStatusOk()
      .body(StartScriptExecutionResponse.class);

    List<Event> eventJsons = GET("events")
      .execute()
      .assertStatusOk()
      .body(new TypeToken<List<Event>>(){}.getType());

    assertTrue(eventJsons.size()>2);
  }

  @Override
  public AsyncHttpServer getNettyServer() {
    return server.asyncHttpServer;
  }
}

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rockscript.engine.*;
import io.rockscript.handlers.CommandHandler;
import io.rockscript.handlers.EventsHandler;
import io.rockscript.netty.router.*;
import io.rockscript.rest.DeployScriptHandler;

import static io.rockscript.command.Command.createCommandsTypeAdapterFactory;
import static io.rockscript.engine.EventJson.createEventJsonTypeAdapterFactory;

public class Server {

  AsyncHttpServer asyncHttpServer;

  public Server(ServerConfiguration serverConfiguration) {
    Gson commonGson = new GsonBuilder()
      .registerTypeAdapterFactory(createCommandsTypeAdapterFactory())
      .registerTypeAdapterFactory(createEventJsonTypeAdapterFactory())
      .create();

    EngineConfiguration engineConfiguration = serverConfiguration.getEngineConfiguration();
    engineConfiguration.eventsGson(commonGson);
    Engine engine = engineConfiguration.build();

    AsyncHttpServerConfiguration asyncHttpServerConfiguration = serverConfiguration
      .getAsyncHttpServerConfiguration()
      .scan(CommandHandler.class)
      .scan(EventsHandler.class)
      .scan(DeployScriptHandler.class)
      .jsonHandler(new JsonHandlerGson(commonGson))
      .context(Engine.class, engine);

    this.asyncHttpServer = new AsyncHttpServer(asyncHttpServerConfiguration);
  }

  public void startup() {
    asyncHttpServer.startup();
  }

  public void shutdown() {
    asyncHttpServer.shutdown();
  }

  public void waitForShutdown() {
    asyncHttpServer.waitForShutdown();
  }
}

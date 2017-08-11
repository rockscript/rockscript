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
import io.rockscript.engine.EngineConfiguration;
import io.rockscript.handlers.CommandHandler;
import io.rockscript.handlers.EventsHandler;
import io.rockscript.netty.router.*;
import io.rockscript.rest.DeployScriptHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.rockscript.command.Command.createCommandsTypeAdapterFactory;
import static io.rockscript.engine.EventJson.createEventJsonTypeAdapterFactory;

public class Server {

  static Logger log = LoggerFactory.getLogger(Server.class);

  AsyncHttpServer asyncHttpServer;

  public Server(ServerConfiguration serverConfiguration) {
    Gson commonGson = new GsonBuilder()
      .registerTypeAdapterFactory(createCommandsTypeAdapterFactory())
      .registerTypeAdapterFactory(createEventJsonTypeAdapterFactory())
      .create();

    EngineConfiguration engineConfiguration = serverConfiguration.getEngineConfiguration();
    engineConfiguration.gson(commonGson);
    ScriptService scriptService = engineConfiguration.build();

    AsyncHttpServerConfiguration asyncHttpServerConfiguration = serverConfiguration
      .getAsyncHttpServerConfiguration()
      .scan(CommandHandler.class)
      .scan(EventsHandler.class)
      .scan(DeployScriptHandler.class)
      .jsonHandler(new JsonHandlerGson(commonGson))
      .context(ScriptService.class, scriptService);

    this.asyncHttpServer = new AsyncHttpServer(asyncHttpServerConfiguration);
  }

  public void startup() {
    log.info(" ____            _     ____            _       _    \n");
    log.info("|  _ \\ ___   ___| | __/ ___|  ___ _ __(_)_ __ | |_  \n");
    log.info("| |_) / _ \\ / __| |/ /\\___ \\ / __| '__| | '_ \\| __| \n");
    log.info("|  _ < (_) | (__|   <  ___) | (__| |  | | |_) | |_  \n");
    log.info("|_| \\_\\___/ \\___|_|\\_\\|____/ \\___|_|  |_| .__/ \\__| \n");
    log.info("                                        |_|         ");
    asyncHttpServer.startup();
    log.info("Server started on "+asyncHttpServer.getPort());
  }

  public void shutdown() {
    asyncHttpServer.shutdown();
  }

  public void waitForShutdown() {
    asyncHttpServer.waitForShutdown();
  }
}

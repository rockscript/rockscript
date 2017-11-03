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
import com.google.gson.reflect.TypeToken;
import io.rockscript.api.*;
import io.rockscript.api.commands.SaveScriptVersionCommand;
import io.rockscript.api.commands.EndActivityCommand;
import io.rockscript.api.commands.RunTestsCommand;
import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.api.queries.EventsQuery;
import io.rockscript.engine.Configuration;
import io.rockscript.engine.DevConfiguration;
import io.rockscript.engine.PingHandler;
import io.rockscript.gson.PolymorphicTypeAdapterFactory;
import io.rockscript.netty.router.AsyncHttpServer;
import io.rockscript.netty.router.AsyncHttpServerConfiguration;
import io.rockscript.netty.router.JsonHandlerGson;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.BindException;
import java.util.ServiceLoader;

import static io.rockscript.Rock.log;

public class Server extends CliCommand {

  static Logger log = LoggerFactory.getLogger(Server.class);

  protected DevConfiguration serviceConfiguration;
  protected ServerConfiguration serverConfiguration;
  protected AsyncHttpServer asyncHttpServer;

  @Override
  protected Options getOptions() {
    Options options = new Options();
    options.addOption("ed", "Add example script and execution on startup");
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
    super.parse(commandLine);
  }

  @Override
  protected void logCommandUsage() {
    log("rock server : Starts the RockScript server");
    log();
    logCommandUsage("rock server [server options]");
    log();
    log("Example:");
    log("  rock server");
    log("Starts the RockScript server on port 3652");
  }

  @Override
  public void execute() {
    configure();
    startup();
    if (commandLine!=null && commandLine.hasOption("ed")) {
      new Deploy()
        .parseArgs("-r", "-n", ".*docs/examples.*\\.rs(t)?", ".")
        .execute();
      new Start()
        .scriptId("sv1")
        .execute();
    }
  }

  protected void configure() {
    Gson commonGson = RockScriptGson.createCommonGson();
    this.serviceConfiguration = createServiceConfiguration();
    this.serverConfiguration = createServerConfiguration();
    this.serviceConfiguration.gson(commonGson);
    CommandExecutorService commandExecutorService = serviceConfiguration.build();
    AsyncHttpServerConfiguration asyncHttpServerConfiguration = createAsyncHttpServerConfiguration(commonGson, commandExecutorService);
    this.asyncHttpServer = new AsyncHttpServer(asyncHttpServerConfiguration);
  }

  protected ServerConfiguration createServerConfiguration() {
    return new ServerConfiguration(serviceConfiguration);
  }

  protected DevConfiguration createServiceConfiguration() {
    return new DevConfiguration();
  }

  protected AsyncHttpServerConfiguration createAsyncHttpServerConfiguration(Gson commonGson, CommandExecutorService commandExecutorService) {
    return serverConfiguration
      .getAsyncHttpServerConfiguration()
      .scan(CommandHandler.class)
      .scan(PingHandler.class)
      .scan(EventsQuery.class)
      .loadAsyncHttpServerModulesFromClassPath()
      .jsonHandler(new JsonHandlerGson(commonGson))
      .context(CommandExecutorService.class, commandExecutorService)
      .context(Configuration.class, serviceConfiguration)
      .defaultResponseHeader("Access-Control-Allow-Origin", "*");
  }

  public static PolymorphicTypeAdapterFactory createCommandsTypeAdapterFactory() {
    PolymorphicTypeAdapterFactory polymorphicTypeAdapterFactory = new PolymorphicTypeAdapterFactory();
    ServiceLoader<CommandsModule> commandModules = ServiceLoader.load(CommandsModule.class);
    for (CommandsModule commandsModule : commandModules) {
      commandsModule.registerCommands(polymorphicTypeAdapterFactory);
    }
    return polymorphicTypeAdapterFactory
      .typeName(new TypeToken<Command>(){}, "command")
      .typeName(SaveScriptVersionCommand.class, "saveScript")
      .typeName(StartScriptExecutionCommand.class, "startScript")
      .typeName(EndActivityCommand.class, "endActivity")
      .typeName(RunTestsCommand.class, "runTests")
      ;
  }

  public void startup() {
    log(" ____            _     ____            _       _    ");
    log("|  _ \\ ___   ___| | __/ ___|  ___ _ __(_)_ __ | |_  ");
    log("| |_) / _ \\ / __| |/ /\\___ \\ / __| '__| | '_ \\| __| ");
    log("|  _ < (_) | (__|   <  ___) | (__| |  | | |_) | |_  ");
    log("|_| \\_\\___/ \\___|_|\\_\\|____/ \\___|_|  |_| .__/ \\__| ");
    log("                                        |_|         ");
    try {
      asyncHttpServer.startup();
      log("Server started on port "+asyncHttpServer.getPort());

    } catch (Throwable t) {
      handleServerStartupException(t);
    }
  }

  protected void handleServerStartupException(Throwable t) {
    if (isPortTakenException(t)) {
      log("ERROR: Port "+asyncHttpServer.getPort()+" is already taken");
    } else {
      log("ERROR: RockScript server could not be started: "+t.getMessage());
    }
    asyncHttpServer.shutdown();
  }

  protected static boolean isPortTakenException(Throwable t) {
    return "Address already in use".equals(t.getMessage())
      && (t instanceof BindException);
  }

  public void shutdown() {
    asyncHttpServer.shutdown();
  }

  public void waitForShutdown() {
    asyncHttpServer.waitForShutdown();
  }

  public AsyncHttpServer getAsyncHttpServer() {
    return asyncHttpServer;
  }

  public CommandExecutorService getCommandExecutorService() {
    return asyncHttpServer.getContext().get(CommandExecutorService.class);
  }
}

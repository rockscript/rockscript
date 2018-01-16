/*
 * Copyright (c) 2018 RockScript.io.
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
package io.rockscript;

import io.rockscript.api.Command;
import io.rockscript.api.CommandHandler;
import io.rockscript.api.Query;
import io.rockscript.api.QueryHandler;
import io.rockscript.api.commands.*;
import io.rockscript.api.queries.*;
import io.rockscript.engine.PingHandler;
import io.rockscript.http.servlet.RequestHandler;
import io.rockscript.service.ImportProvider;
import io.rockscript.service.http.HttpService;

import java.util.*;

public class Configuration {

  public static final String CFG_KEY_ENGINE = "engine";
  public static final String CFG_VALUE_ENGINE_TEST = "test";
  public static final String CFG_KEY_EXAMPLES = "examples";

  boolean test = false;
  boolean examples = false;
  protected List<Query> queries = new ArrayList<>();
  protected List<Command> commands = new ArrayList<>();
  protected List<EngineListener> engineListeners = new ArrayList<>();
  protected List<EnginePlugin> enginePlugins = new ArrayList<>();
  protected List<RequestHandler> requestHandlers = new ArrayList<>();
  protected List<ImportProvider> importProviders = new ArrayList<>();

  public Configuration configureArgs(String[] args) {
    return configureProperties(parseConfigurationArgs(args));
  }

  public Configuration configureProperties(Map<String,String> configurationProperties) {
    if (CFG_VALUE_ENGINE_TEST.equals(configurationProperties.get(CFG_KEY_ENGINE))) {
      configureTest();
    }
    if (configurationProperties.containsKey(CFG_KEY_EXAMPLES)) {
      configureExamples();
    }
    return this;
  }

  /** Parses args like this
   * For args like this key=value => configuration.put(key, value);
   * For args not having '=' configuration.put(arg, null); */
  public static Map<String, String> parseConfigurationArgs(String[] args) {
    Map<String, String> configuration = new LinkedHashMap<>();
    if (args!=null && args.length>0) {
      for (String arg: args) {
        int separatorIndex = arg.indexOf('=');
        String key = null;
        String value = null;
        if (separatorIndex!=-1) {
          key = arg.substring(0, separatorIndex);
          value = arg.length()>separatorIndex ? arg.substring(separatorIndex+1) : null;
        } else {
          key = arg;
        }
        configuration.put(key, value);
      }
    }
    return configuration;
  }

  public Configuration() {
    initialize();
  }

  protected void initialize() {
    initializeQueries();
    initializeCommands();
    initializeEngineListeners();
    initializeRequestHandlers();
    initializeImportProviders();
  }

  protected void initializeQueries() {
    addQuery(new ScriptsQuery());
    addQuery(new ScriptVersionsQuery());
    addQuery(new ScriptExecutionQuery());
    addQuery(new ScriptExecutionsQuery());
    addQuery(new EventsQuery());
  }

  /** can be used by plugins to add queries */
  public Configuration addQuery(Query query) {
    queries.add(query);
    return this;
  }

  /** can be customized by sub-configuration types to replace or add commands */
  protected void initializeCommands() {
    addCommand(new SaveScriptVersionCommand());
    addCommand(new DeployScriptVersionCommand());
    addCommand(new StartScriptExecutionCommand());
    addCommand(new EndServiceFunctionCommand());
    addCommand(new RunTestsCommand());
  }

  /** can be used by plugins to add commands */
  public Configuration addCommand(Command command) {
    commands.add(command);
    return this;
  }

  /** can be customized by sub-configuration types to replace or add {@link EngineListener}s */
  protected void initializeEngineListeners() {
  }

  /** can be used by plugins to add commands */
  public Configuration addEngineListener(EngineListener engineListener) {
    engineListeners.add(engineListener);
    return this;
  }

  public void initializeEnginePlugins() {
    ServiceLoader<EnginePlugin> pluginLoader = ServiceLoader.load(EnginePlugin.class);
    for (EnginePlugin plugin : pluginLoader) {
      enginePlugins.add(plugin);
    }
  }

  public Configuration addEnginePlugin(EnginePlugin enginePlugin) {
    enginePlugins.add(enginePlugin);
    return this;
  }

  protected void initializeRequestHandlers() {
    addRequestHandler(new CommandHandler());
    addRequestHandler(new QueryHandler());
    addRequestHandler(new PingHandler());
    addRequestHandler(new FileHandler());
  }

  public Configuration addRequestHandler(RequestHandler requestHandler) {
    requestHandlers.add(requestHandler);
    return this;
  }

  protected void initializeImportProviders() {
    addImportProvider(new HttpService());
  }

  public Configuration addImportProvider(ImportProvider importProvider) {
    this.importProviders.add(importProvider);
    return this;
  }

  public Configuration configureTest() {
    this.test = true;
    return this;
  }

  public Configuration configureExamples() {
    this.examples = true;
    return this;
  }

  // build ////////////////////////////////////////////////////////////////////////////////////////////////

  public Engine build() {
    return new Engine(this);
  }

  // getters //////////////////////////////////////////////////////////////////////////////////////////////

  public boolean isTest() {
    return test;
  }

  public boolean isExamples() {
    return examples;
  }

  public List<ImportProvider> getImportProviders() {
    return importProviders;
  }

  public List<Query> getQueries() {
    return queries;
  }

  public List<Command> getCommands() {
    return commands;
  }

  public List<EngineListener> getEngineListeners() {
    return engineListeners;
  }

  public List<EnginePlugin> getEnginePlugins() {
    return enginePlugins;
  }

  public List<RequestHandler> getRequestHandlers() {
    return requestHandlers;
  }
}

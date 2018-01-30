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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.rockscript.api.*;
import io.rockscript.api.commands.*;
import io.rockscript.api.events.*;
import io.rockscript.api.queries.*;
import io.rockscript.engine.ImportObjectSerializer;
import io.rockscript.engine.PingHandler;
import io.rockscript.engine.ServiceFunctionSerializer;
import io.rockscript.engine.impl.*;
import io.rockscript.engine.job.InMemoryJobExecutor;
import io.rockscript.engine.job.InMemoryJobStore;
import io.rockscript.engine.job.JobService;
import io.rockscript.examples.ExamplesHandler;
import io.rockscript.examples.ExamplesLoader;
import io.rockscript.gson.PolymorphicTypeAdapterFactory;
import io.rockscript.http.client.HttpClient;
import io.rockscript.http.servlet.RequestHandler;
import io.rockscript.service.ImportObject;
import io.rockscript.service.ImportProvider;
import io.rockscript.service.ImportResolver;
import io.rockscript.service.ServiceFunction;
import io.rockscript.service.http.HttpService;
import io.rockscript.test.TestJobExecutor;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    initializeEnginePlugins();
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
    Engine engine = createEngine();

    enginePlugins.forEach(plugin->{
      plugin.configure(Configuration.this, engine);
      if (plugin instanceof EngineListener) {
        engineListeners.add((EngineListener) plugin);
      }
    });

    if (isExamples()) {
      addEngineListener(new ExamplesLoader());
      // The ExamplesHandler must be addd before the FileHandler
      List<RequestHandler> requestHandlers = getRequestHandlers();
      requestHandlers.add(requestHandlers.size()-2, new ExamplesHandler(engine));
    }

    engine.engineLogStore = new EngineLogStore(engine);

    if (isTest()) {
      engine.executor = MonitoringExecutor.createTest(engine.engineLogStore);
      engine.jobExecutor = new TestJobExecutor(engine);
    } else {
      engine.executor = MonitoringExecutor.createDefault(engine.engineLogStore);
      engine.jobExecutor = new InMemoryJobExecutor(engine);
    }

    engine.eventDispatcher = createEventDispatcher(engine);
    engine.scriptExecutionStore = new ScriptExecutionStore(engine);
    engine.scriptStore = new ScriptStore(engine);
    engine.scriptParser = new ScriptParser(engine);
    engine.jobIdGenerator = new TestIdGenerator(engine, "j");
    engine.scriptIdGenerator = new TestIdGenerator(engine, "s");
    engine.scriptVersionIdGenerator = new TestIdGenerator(engine, "sv");
    engine.scriptExecutionIdGenerator = new TestIdGenerator(engine, "se");
    engine.lockService = new LockServiceImpl(engine);
    engine.lockOperationExecutor = new LockOperationExecutorImpl(engine);
    engine.jobService = new JobService(engine);
    engine.jobStore = new InMemoryJobStore(engine);
    engine.converter = new Converter(engine);
    engine.context = new HashMap<>();

    engine.commands = commands;
    engine.queries = queries;
    engine.engineListeners = engineListeners;
    engine.requestHandlers = requestHandlers;

    // Initialize AbstractRequestHandlers
    engine.requestHandlers.forEach(requestHandler->{
      if (requestHandler instanceof AbstractRequestHandler) {
        ((AbstractRequestHandler)requestHandler).setEngine(engine);
      }
    });

    // Requires plugins to be initialized
    engine.gson = buildGson();
    engine.httpClient = new HttpClient(engine.gson);
    engine.importResolver = new ImportResolver(engine, importProviders);

    engine.scanMemberFieldsForEngineListeners();
    engine.throwIfNotProperlyInitialized();
    return engine;
  }

  protected Engine createEngine() {
    return new Engine();
  }

  protected EventDispatcher createEventDispatcher(Engine engine) {
    return new EventDispatcher(engine);
  }

  public Gson buildGson() {
    return new GsonBuilder()
      .registerTypeAdapterFactory(createCommandTypeAdapterFactory())
      .registerTypeAdapterFactory(createQueryTypeAdapterFactory())
      .registerTypeAdapterFactory(createEventJsonTypeAdapterFactory())
      .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
      .registerTypeHierarchyAdapter(ServiceFunction.class, new ServiceFunctionSerializer())
      .registerTypeHierarchyAdapter(ImportObject.class, new ImportObjectSerializer())
      .setPrettyPrinting()
      .create();
  }

  protected PolymorphicTypeAdapterFactory createCommandTypeAdapterFactory() {
    PolymorphicTypeAdapterFactory polymorphicTypeAdapterFactory = new PolymorphicTypeAdapterFactory();
    polymorphicTypeAdapterFactory.typeName(new TypeToken<Command>(){}, "command");
    getCommands().stream()
      .forEach(command->polymorphicTypeAdapterFactory.typeName(command.getClass(),command.getType()));
    return polymorphicTypeAdapterFactory;
  }

  protected PolymorphicTypeAdapterFactory createQueryTypeAdapterFactory() {
    PolymorphicTypeAdapterFactory polymorphicTypeAdapterFactory = new PolymorphicTypeAdapterFactory();
    polymorphicTypeAdapterFactory.typeName(new TypeToken<Query>() {}, "query");
    getQueries().stream()
      .forEach(query->polymorphicTypeAdapterFactory.typeName(query.getClass(),query.getName()));
    return polymorphicTypeAdapterFactory;
  }

  protected static PolymorphicTypeAdapterFactory createEventJsonTypeAdapterFactory() {
    return new PolymorphicTypeAdapterFactory()
      .typeName(new TypeToken<Event>(){},                       "event") // abstract type 'event' should not be used, but is specified because required by PolymorphicTypeAdapterFactory
      .typeName(new TypeToken<ExecutionEvent>(){},              "executionEvent") // abstract type 'event' should not be used, but is specified because required by PolymorphicTypeAdapterFactory
      .typeName(new TypeToken<ServiceFunctionStartedEvent>(){}, "serviceFunctionStarted")
      .typeName(new TypeToken<ServiceFunctionRetriedEvent>(){}, "serviceFunctionRetried")
      .typeName(new TypeToken<ServiceFunctionWaitedEvent>(){},  "serviceFunctionWaited")
      .typeName(new TypeToken<ServiceFunctionEndedEvent>(){},   "serviceFunctionEnded")
      .typeName(new TypeToken<ServiceFunctionFailedEvent>(){},   "serviceFunctionFailed")
      .typeName(new TypeToken<ScriptEndedEvent>(){},            "scriptEnded")
      .typeName(new TypeToken<ScriptStartedEvent>(){},          "scriptStarted")
      .typeName(new TypeToken<VariableCreatedEvent>(){},        "variableCreated")
      .typeName(new TypeToken<ScriptExecutionErrorEvent>(){},   "scriptExecutionError")
      .typeName(new TypeToken<ScriptVersionSavedEvent>(){},     "scriptVersionSaved")
      ;
  }

  static class InstantTypeAdapter extends TypeAdapter<Instant> {
    static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME
      .withZone(ZoneId.of("UTC"));
    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
      if (value!=null) {
        out.value(ISO_FORMATTER.format(value));
      } else {
        out.nullValue();
      }
    }
    @Override
    public Instant read(JsonReader in) throws IOException {
      String isoText = in.nextString();
      return OffsetDateTime
        .parse(isoText, ISO_FORMATTER)
        .toInstant();
    }
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

/*
 * Copyright (c) 2017 RockScript.io.
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
import io.rockscript.api.Command;
import io.rockscript.api.CommandHandler;
import io.rockscript.api.Query;
import io.rockscript.api.QueryHandler;
import io.rockscript.api.commands.*;
import io.rockscript.api.events.*;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.api.queries.*;
import io.rockscript.engine.EngineException;
import io.rockscript.engine.ImportObjectSerializer;
import io.rockscript.engine.PingHandler;
import io.rockscript.engine.ServiceFunctionSerializer;
import io.rockscript.engine.impl.*;
import io.rockscript.engine.job.*;
import io.rockscript.examples.ExamplesHandler;
import io.rockscript.gson.PolymorphicTypeAdapterFactory;
import io.rockscript.http.client.HttpClient;
import io.rockscript.http.servlet.RequestHandler;
import io.rockscript.service.ImportObject;
import io.rockscript.service.ImportProvider;
import io.rockscript.service.ImportResolver;
import io.rockscript.service.ServiceFunction;
import io.rockscript.service.http.HttpService;
import io.rockscript.test.TestExecutor;
import io.rockscript.test.TestJobExecutor;
import io.rockscript.util.Io;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings("unchecked")
public class Engine {

  static Logger log = LoggerFactory.getLogger(Engine.class);

  /** annotation that should be used for optional Engine fields */
  @Retention(RetentionPolicy.RUNTIME)
  private @interface Optional {
  }

  public static final String CFG_KEY_ENGINE = "engine";
  public static final String CFG_VALUE_ENGINE_TEST = "test";
  public static final String CFG_KEY_EXAMPLES = "examples";

  protected boolean created;
  protected boolean started;

  protected Map<String,String> configuration;
  protected IdGenerator scriptIdGenerator;
  protected IdGenerator scriptVersionIdGenerator;
  protected IdGenerator scriptExecutionIdGenerator;
  protected IdGenerator jobIdGenerator;
  protected EventDispatcher eventDispatcher;
  protected ScriptStore scriptStore;
  protected EngineLogStore engineLogStore;
  protected ScriptExecutionStore scriptExecutionStore;
  protected ScriptParser scriptParser;
  protected LockOperationExecutor lockOperationExecutor;
  protected LockService lockService;
  protected JobService jobService;
  protected JobStore jobStore;
  protected JobExecutor jobExecutor;
  protected ImportResolver importResolver;
  protected Executor executor;
  protected Gson gson;
  protected HttpClient httpClient;
  protected Map<String,Class<? extends Command>> commandTypes = new HashMap<>();
  protected Map<String,Class<? extends Query>> queryTypes = new HashMap<>();
  @Deprecated // I think this can be deleted, but now is not a good time to check it
  protected Map<String,Object> objects = new HashMap<>();
  protected Map<String,ImportProvider> importProviders = new HashMap<>();
  protected CommandHandler commandHandler;
  protected QueryHandler queryHandler;
  protected PingHandler pingHandler;
  protected FileHandler fileHandler;
  @Optional
  protected ExamplesHandler examplesHandler;

  protected List<EnginePlugin> plugins = new ArrayList<>();
  protected List<EngineListener> engineListeners = new ArrayList<>();

  public Engine(String[] args) {
    this(parseConfigurationArgs(args));
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

  public Engine(Map<String,String> configuration) {
    this.configuration = configuration;
    this.eventDispatcher = new EventDispatcher(this);
    this.scriptExecutionStore = new ScriptExecutionStore(this);
    this.scriptStore = new ScriptStore(this);
    this.engineLogStore = new EngineLogStore(this);
    this.scriptParser = new ScriptParser(this);
    this.jobIdGenerator = new TestIdGenerator(this, "j");
    this.scriptIdGenerator = new TestIdGenerator(this, "s");
    this.scriptVersionIdGenerator = new TestIdGenerator(this, "sv");
    this.scriptExecutionIdGenerator = new TestIdGenerator(this, "se");
    this.lockService = new LockServiceImpl(this);
    this.lockOperationExecutor = new LockOperationExecutorImpl(this);
    this.commandHandler = new CommandHandler(this);
    this.queryHandler = new QueryHandler(this);
    this.pingHandler = new PingHandler(this);

    this.fileHandler = createFileHandler();
    this.executor = new MonitoringExecutor(engineLogStore, createExecutor());
    this.jobService = createJobService();
    this.jobStore = createJobStore();
    this.jobExecutor = createJobExecutor();

    this.importResolver = new ImportResolver(this);
    importProvider(new HttpService());

    this.queryTypes = new HashMap<>();
    query(new ScriptsQuery());
    query(new ScriptVersionsQuery());
    query(new ScriptExecutionQuery());
    query(new ScriptExecutionsQuery());
    query(new EventsQuery());

    this.commandTypes = new HashMap<>();
    command(new SaveScriptVersionCommand());
    command(new DeployScriptVersionCommand());
    command(new StartScriptExecutionCommand());
    command(new EndServiceFunctionCommand());
    command(new RunTestsCommand());

    ServiceLoader<EnginePlugin> pluginLoader = ServiceLoader.load(EnginePlugin.class);
    for (EnginePlugin plugin : pluginLoader) {
      plugin.created(this);
    }

    plugins.forEach(plugin->plugin.created(this));

    this.gson = createGson();
    this.httpClient = new HttpClient(gson);
    throwIfNotProperlyConfigured();
    this.created = true;
  }

  protected FileHandler createFileHandler() {
    return new FileHandler(this);
  }

  protected Executor createExecutor() {
    if (isTestEngine()) {
      return new TestExecutor();
    }
    return Executors.newWorkStealingPool();
  }

  protected JobService createJobService() {
    return new JobService(this);
  }

  protected JobStore createJobStore() {
    return new InMemoryJobStore(this);
  }

  protected JobExecutor createJobExecutor() {
    if (isTestEngine()) {
      return new TestJobExecutor(this);
    } else {
      InMemoryJobExecutor jobExecutor = new InMemoryJobExecutor(this);
      engineListeners.add(jobExecutor);
      return jobExecutor;
    }
  }

  protected boolean isTestEngine() {
    return CFG_VALUE_ENGINE_TEST.equals(configuration.get(CFG_KEY_ENGINE));
  }

  public Engine importProvider(ImportProvider importProvider) {
    importResolver.add(importProvider);
    return this;
  }

  public Engine start() {
    if (!started) {
      started = true;
      engineListeners.forEach(listener->listener.engineStarts(this));
      if (configuration.containsKey(CFG_KEY_EXAMPLES)) {
        // The executor juggling is to ensure that the rockscript startup logging
        // happens after the examples are loaded
        Executor original = this.executor;
        this.executor = new TestExecutor();
        initializeExamples();
        this.executor = original;
      }
    }
    return this;
  }

  private void initializeExamples() {
    this.examplesHandler = new ExamplesHandler(this);
    List<ScriptVersion> exampleScripts = new ArrayList<>();
    exampleScripts.add(deployExampleScript("examples/local-error.rs"));
    exampleScripts.add(deployExampleScript("examples/local-retry.rs"));
    exampleScripts.add(deployExampleScript("examples/star-wars.rs"));
    exampleScripts.add(deployExampleScript("examples/chuck-norris.rs"));

    List<ScriptExecutionResponse> exampleExecutions = new ArrayList<>();
    exampleExecutions.add(startExampleScriptExecution("examples/star-wars.rs"));
    exampleExecutions.add(startExampleScriptExecution("examples/chuck-norris.rs"));

    log.debug("Examples initialized: "+exampleScripts.size()+" example scripts and "+exampleExecutions.size()+" script executions available:");
    exampleScripts.forEach(script->log.debug("  Script "+script.getId()+" : "+script.getScriptName()));
    exampleExecutions.forEach(execution->log.debug("  Script execution "+execution.getScriptExecutionId()+" : "+execution.getEngineScriptExecution().getEngineScript().getScriptVersion().getScriptName()));
  }

  private ScriptExecutionResponse startExampleScriptExecution(String scriptName) {
    return new StartScriptExecutionCommand()
      .scriptName(scriptName)
      .execute(this);
  }

  private ScriptVersion deployExampleScript(String resource) {
    String scriptText = Io.getResourceAsString(resource);
    return new DeployScriptVersionCommand()
      .scriptName(resource)
      .scriptText(scriptText)
      .execute(this);
  }

  public void stop() {
    if (started) {
      engineListeners.forEach(plugin->plugin.engineStops(this));
      started = false;
    }
  }

  private Gson createGson() {
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
    commandTypes.entrySet()
      .forEach(entry->polymorphicTypeAdapterFactory.typeName(entry.getValue(),entry.getKey()));
    return polymorphicTypeAdapterFactory;
  }

  protected PolymorphicTypeAdapterFactory createQueryTypeAdapterFactory() {
    PolymorphicTypeAdapterFactory polymorphicTypeAdapterFactory = new PolymorphicTypeAdapterFactory();
    polymorphicTypeAdapterFactory.typeName(new TypeToken<Query>() {}, "query");
    queryTypes.entrySet()
      .forEach(entry->polymorphicTypeAdapterFactory.typeName(entry.getValue(),entry.getKey()));
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

  protected void throwIfNotProperlyConfigured() {
    Class<?> clazz = getClass();
    while (clazz!=Object.class) {
      for (Field field: clazz.getDeclaredFields()) {
        if (field.getAnnotation(Optional.class)==null) {
          Object value = null;
          try {
            field.setAccessible(true);
            value = field.get(this);
          } catch (IllegalAccessException e) {
            throw new EngineException(e);
          }
          EngineException.throwIfNull(value, "Engine field '%s' is null", field.getName());
        }
      }
      clazz = clazz.getSuperclass();
    }
  }

  public boolean isStarted() {
    return started;
  }

  public Engine object(String key, Object value) {
    objects.put(key, value);
    return this;
  }

  public Engine object(Class<?> key, Object value) {
    if (value!=null && key!=Object.class) {
      objects.put(key.getName(), value);
      for (Class<?> interfaceClass: key.getInterfaces()) {
        object(interfaceClass, value);
      }
      object(key.getSuperclass(), value);
    }
    return this;
  }

  public Engine object(Object value) {
    if (value!=null) {
      object(value.getClass(), value);
    }
    return this;
  }

  public <T> T getObject(String key) {
    return (T) objects.get(key);
  }

  public <T> T getObject(Class<T> clazz) {
    return (T) objects.get(clazz.getName());
  }

  public Map<String,Class<? extends Query>> getQueryTypes() {
    return this.queryTypes;
  }

  public Engine query(Query query) {
    if (created) {
      throw new RuntimeException("Queries can only be added when the Engine is created. Consider using "+EnginePlugin.class.getSimpleName());
    }
    this.queryTypes.put(query.getName(), query.getClass());
    return this;
  }

  public Map<String, Class<? extends Command>> getCommandTypes() {
    return commandTypes;
  }

  public Engine command(Command command) {
    if (created) {
      throw new RuntimeException("Command can only be added when the Engine is created. Consider using "+EnginePlugin.class.getSimpleName());
    }
    this.commandTypes.put(command.getType(), command.getClass());
    return this;
  }

  public void addEngineListener(EngineListener engineListener) {
    engineListeners.add(engineListener);
  }

  public IdGenerator getScriptIdGenerator() {
    return scriptIdGenerator;
  }

  public IdGenerator getScriptExecutionIdGenerator() {
    return scriptExecutionIdGenerator;
  }

  public IdGenerator getJobIdGenerator() {
    return jobIdGenerator;
  }

  public EventDispatcher getEventDispatcher() {
    return eventDispatcher;
  }

  public ScriptExecutionStore getScriptExecutionStore() {
    return scriptExecutionStore;
  }

  public ScriptStore getScriptStore() {
    return scriptStore;
  }

  public LockOperationExecutor getLockOperationExecutor() {
    return lockOperationExecutor;
  }

  public ImportResolver getImportResolver() {
    return importResolver;
  }

  public Gson getGson() {
    return gson;
  }

  public Executor getExecutor() {
    return executor;
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }

  public JobService getJobService() {
    return jobService;
  }

  public IdGenerator getScriptVersionIdGenerator() {
    return scriptVersionIdGenerator;
  }

  public EngineLogStore getEngineLogStore() {
    return engineLogStore;
  }

  public LockService getLockService() {
    return lockService;
  }

  public CommandHandler getCommandHandler() {
    return commandHandler;
  }

  public QueryHandler getQueryHandler() {
    return queryHandler;
  }

  public PingHandler getPingHandler() {
    return pingHandler;
  }

  public RequestHandler getExamplesHandler() {
    return examplesHandler;
  }

  public FileHandler getFileHandler() {
    return fileHandler;
  }

  public ScriptParser getScriptParser() {
    return scriptParser;
  }

  public JobStore getJobStore() {
    return jobStore;
  }

  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }
}

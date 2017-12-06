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
import io.rockscript.service.ServiceFunction;
import io.rockscript.service.ImportObject;
import io.rockscript.service.ImportProvider;
import io.rockscript.service.ImportResolver;
import io.rockscript.service.http.HttpService;
import io.rockscript.api.Command;
import io.rockscript.api.Query;
import io.rockscript.api.commands.*;
import io.rockscript.api.queries.ScriptExecutionQuery;
import io.rockscript.engine.ServiceFunctionSerializer;
import io.rockscript.engine.EngineException;
import io.rockscript.engine.ImportObjectSerializer;
import io.rockscript.engine.impl.*;
import io.rockscript.engine.impl.EventListener;
import io.rockscript.engine.job.JobService;
import io.rockscript.gson.PolymorphicTypeAdapterFactory;
import io.rockscript.http.client.Http;

import java.io.IOException;
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

  protected IdGenerator scriptIdGenerator;
  protected IdGenerator scriptVersionIdGenerator;
  protected IdGenerator scriptExecutionIdGenerator;
  protected IdGenerator jobIdGenerator;

  protected EventStore eventStore;
  protected ScriptStore scriptStore;
  protected EngineLogStore engineLogStore;
  protected EventListener eventListener;
  protected LockOperationExecutor lockOperationExecutor;
  protected LockService lockService;
  protected ImportResolver importResolver;
  protected Executor executor;
  protected Gson gson;
  protected Http http;
  protected JobService jobService;
  protected Map<String,Class<? extends Command>> commandTypes = new HashMap<>();
  protected Map<String,Class<? extends Query>> queryTypes = new HashMap<>();
  @Deprecated // I think this can be deleted, but now is not a good time to check it
  protected Map<String,Object> objects = new HashMap<>();
  protected Map<String,ImportProvider> importProviders = new HashMap<>();
  protected List<EnginePlugin> plugins = new ArrayList<>();

  public Engine() {
    this.eventStore = new EventStore(this);
    this.scriptStore = new ScriptStore(this);
    this.engineLogStore = new EngineLogStore(this);
    this.eventListener = new EventLogger(this, eventStore);
    this.jobIdGenerator = new TestIdGenerator(this, "j");
    this.scriptIdGenerator = new TestIdGenerator(this, "s");
    this.scriptVersionIdGenerator = new TestIdGenerator(this, "sv");
    this.scriptExecutionIdGenerator = new TestIdGenerator(this, "se");
    this.lockService = new LockServiceImpl(this);
    this.lockOperationExecutor = new LockOperationExecutorImpl(this);
    this.jobService = createJobService();

    this.importResolver = new ImportResolver(this);
    importProvider(new HttpService());

    this.queryTypes = new HashMap<>();
    queryType(new ScriptExecutionQuery());

    this.commandTypes = new HashMap<>();
    commandType(new SaveScriptVersionCommand());
    commandType(new DeployScriptVersionCommand());
    commandType(new StartScriptExecutionCommand());
    commandType(new EndServiceFunctionCommand());
    commandType(new RunTestsCommand());

    ServiceLoader<EnginePlugin> pluginLoader = ServiceLoader.load(EnginePlugin.class);
    for (EnginePlugin plugin : pluginLoader) {
      plugins.add(plugin);
    }

    plugins.forEach(plugin->plugin.created(this));
  }

  protected JobService createJobService() {
    return new JobService(this);
  }

  public Engine importProvider(ImportProvider importProvider) {
    importResolver.add(importProvider);
    return this;
  }

  public Engine start() {
    this.gson = createGson();
    this.executor = new LoggingExecutor(engineLogStore, createExecutor());
    this.http = new Http(gson);
    throwIfNotProperlyConfigured();
    plugins.forEach(plugin->plugin.start(this));
    this.jobService.startup();
    return this;
  }

  public void stop() {
    this.jobService.shutdown();
    plugins.forEach(plugin->plugin.stop(this));
  }

  protected Gson createGson() {
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
      .typeName(new TypeToken<ServiceFunctionStartingEvent>(){},"serviceFunctionStarting")
      .typeName(new TypeToken<ServiceFunctionRetryingEvent>(){},"serviceFunctionRetrying")
      .typeName(new TypeToken<ServiceFunctionWaitingEvent>(){}, "serviceFunctionWaiting")
      .typeName(new TypeToken<ServiceFunctionEndedEvent>(){},   "serviceFunctionEnd")
      .typeName(new TypeToken<ServiceFunctionErrorEvent>(){},   "serviceFunctionError")
      .typeName(new TypeToken<ScriptEndedEvent>(){},            "scriptEnded")
      .typeName(new TypeToken<ScriptStartedEvent>(){},          "scriptStarted")
      .typeName(new TypeToken<VariableCreatedEvent>(){},        "variableCreated")
      .typeName(new TypeToken<ScriptExecutionErrorEvent>(){},   "scriptExecutionError")
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

  protected Executor createExecutor() {
    return Executors.newWorkStealingPool();
  }

  protected void throwIfNotProperlyConfigured() {
    for (Field field: getClass().getDeclaredFields()) {
      Object value = null;
      try {
        field.setAccessible(true);
        value = field.get(this);
      } catch (IllegalAccessException e) {
        throw new EngineException(e);
      }
      EngineException.throwIfNull(value, "ServiceLocator field '%s' is null", field.getName());
    }
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

  public Engine queryType(Query query) {
    this.queryTypes.put(query.getType(), query.getClass());
    return this;
  }

  public Map<String, Class<? extends Command>> getCommandTypes() {
    return commandTypes;
  }

  public Engine commandType(Command command) {
    this.commandTypes.put(command.getType(), command.getClass());
    return this;
  }

  public Engine gson(Gson gson) {
    this.gson = gson;
    return this;
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

  public EventStore getEventStore() {
    return eventStore;
  }

  public ScriptStore getScriptStore() {
    return scriptStore;
  }

  public EventListener getEventListener() {
    return eventListener;
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

  public Http getHttp() {
    return http;
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
}

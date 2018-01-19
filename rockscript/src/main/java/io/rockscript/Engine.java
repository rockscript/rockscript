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
import io.rockscript.api.AbstractRequestHandler;
import io.rockscript.api.Command;
import io.rockscript.api.Query;
import io.rockscript.api.events.*;
import io.rockscript.engine.EngineException;
import io.rockscript.engine.ImportObjectSerializer;
import io.rockscript.engine.ServiceFunctionSerializer;
import io.rockscript.engine.impl.*;
import io.rockscript.engine.job.*;
import io.rockscript.examples.ExamplesHandler;
import io.rockscript.examples.ExamplesLoader;
import io.rockscript.gson.PolymorphicTypeAdapterFactory;
import io.rockscript.http.client.HttpClient;
import io.rockscript.http.servlet.RequestHandler;
import io.rockscript.service.ImportObject;
import io.rockscript.service.ImportResolver;
import io.rockscript.service.ServiceFunction;
import io.rockscript.test.TestJobExecutor;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@SuppressWarnings("unchecked")
public class Engine {

  static Logger log = LoggerFactory.getLogger(Engine.class);

  /** annotation that should be used for optional Engine fields */
  @Retention(RetentionPolicy.RUNTIME)
  private @interface Optional {
  }

  protected boolean started;

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
  protected Converter converter;
  protected Map<Object,Object> context;

  protected List<Command> commands;
  protected List<Query> queries;
  protected List<EngineListener> engineListeners;
  protected List<RequestHandler> requestHandlers;

  public Engine() {
    // memberfields are initialized in Configuration.build()
  }

  public Engine(Engine other) {
    Class<?> clazz = getClass();
    while (clazz!=Object.class) {
      for (Field field: clazz.getDeclaredFields()) {
        Object value = null;
        try {
          field.setAccessible(true);
          if (field.getDeclaringClass().isAssignableFrom(other.getClass())) {
            value = field.get(other);
            field.set(this, value);
          }
        } catch (IllegalAccessException e) {
          throw new EngineException(e);
        }
      }
      clazz = clazz.getSuperclass();
    }
  }

  protected void scanMemberFieldsForEngineListeners() {
    Class<?> clazz = getClass();
    while (clazz!=Object.class) {
      for (Field field: clazz.getDeclaredFields()) {
        Object value = null;
        try {
          field.setAccessible(true);
          value = field.get(this);
          if (value instanceof EngineListener) {
            if (!engineListeners.contains(value)) {
              engineListeners.add((EngineListener) value);
            }
          }
        } catch (IllegalAccessException e) {
          throw new EngineException(e);
        }
      }
      clazz = clazz.getSuperclass();
    }
  }

  protected void throwIfNotProperlyInitialized() {
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

  public Engine start() {
    if (!started) {
      started = true;
      engineListeners.forEach(listener->listener.engineStarts(this));
    }
    return this;
  }

  public void stop() {
    if (started) {
      engineListeners.forEach(plugin->plugin.engineStops(this));
      started = false;
    }
  }

  public boolean isStarted() {
    return started;
  }

  public Map<Object, Object> getContext() {
    return context;
  }

  public List<Query> getQueries() {
    return queries;
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

  public ScriptParser getScriptParser() {
    return scriptParser;
  }

  public JobStore getJobStore() {
    return jobStore;
  }

  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }

  public List<RequestHandler> getRequestHandlers() {
    return requestHandlers;
  }

  public Converter getConverter() {
    return converter;
  }
}

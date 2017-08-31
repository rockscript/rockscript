/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
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

package io.rockscript.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rockscript.EngineException;
import io.rockscript.ScriptService;
import io.rockscript.activity.Activity;
import io.rockscript.activity.ImportResolver;
import io.rockscript.activity.http.ActivityContext;
import io.rockscript.engine.*;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;

import static io.rockscript.engine.Event.createEventJsonTypeAdapterFactory;

public abstract class Configuration implements ActivityContext {

  protected EventStore eventStore;
  protected ScriptStore scriptStore;
  protected EventListener eventListener;
  protected IdGenerator scriptIdGenerator;
  protected IdGenerator scriptExecutionIdGenerator;
  protected Engine engine;
  protected ImportResolver importResolver;
  protected Executor executor;
  protected Gson gson;

  public Configuration() {
    this.eventStore = new EventStore(this);
    this.scriptStore = new ScriptStore(this);
    this.eventListener = new EventLogger(this, eventStore);
    this.scriptIdGenerator = new TestIdGenerator(this, "s");
    this.scriptExecutionIdGenerator = new TestIdGenerator(this, "se");
    this.engine = new LocalEngine(this);
    this.importResolver = new ImportResolver(this);
  }

  public ScriptService build() {
    if (gson==null) {
      gson = createDefaultGson();
    }
    throwIfNotProperlyConfigured();
    return new ScriptServiceImpl(this);
  }

  private Gson createDefaultGson() {
    return new GsonBuilder()
      .registerTypeAdapterFactory(createEventJsonTypeAdapterFactory())
      .registerTypeHierarchyAdapter(Activity.class, new ActivitySerializer())
      .disableHtmlEscaping()
      // .setPrettyPrinting()
      .create();
  }

  public void throwIfNotProperlyConfigured() {
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

  public Configuration gson(Gson gson) {
    this.gson = gson;
    return this;
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

  public IdGenerator getScriptIdGenerator() {
    return scriptIdGenerator;
  }

  public IdGenerator getScriptExecutionIdGenerator() {
    return scriptExecutionIdGenerator;
  }

  public Engine getEngine() {
    return engine;
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
}

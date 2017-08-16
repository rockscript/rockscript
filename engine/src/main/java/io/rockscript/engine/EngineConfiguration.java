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

package io.rockscript.engine;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.muoncore.MultiTransportMuon;
import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.eventstore.TestEventStore;
import io.muoncore.memory.discovery.InMemDiscovery;
import io.muoncore.memory.transport.InMemTransport;
import io.rockscript.ScriptService;
import io.rockscript.action.http.EngineContext;
import io.rockscript.engine.test.TestIdGenerator;

import static io.rockscript.engine.EventJson.createEventJsonTypeAdapterFactory;

public abstract class EngineConfiguration implements EngineContext {

  // used in throwIfNotProperlyConfigured
  private static Set<String> OPTIONAL_FIELDS = new HashSet<>();

  protected EventStore eventStore;
  protected ScriptStore scriptStore;
  protected EventListener eventListener;
  protected IdGenerator scriptIdGenerator;
  protected IdGenerator scriptExecutionIdGenerator;
  protected Engine engine;
  protected ImportResolver importResolver;
  protected Executor executor;
  private Muon muon;

  private TestEventStore testEventStore;

  static { OPTIONAL_FIELDS.add("gson"); }
  protected Gson gson;
  protected ScriptService scriptService;

  public EngineConfiguration() {
    setupLocalMuon();
    this.eventStore = new EventStore(this, muon);
    this.scriptStore = new ScriptStore(this);
    this.eventListener = this.eventStore;
    this.scriptIdGenerator = new TestIdGenerator(this, "s");
    this.scriptExecutionIdGenerator = new TestIdGenerator(this, "se");
    this.engine = new LocalEngine(this);
  }

  private void setupLocalMuon() {
    AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("rockscript")
            .build();
    //if using standard transports (eg AMQP), and an externally managed event store this works fine
//    this.muon = MuonBuilder.withConfig(config).build();

    //For example setup, use an in mem event store and a muon configured to be in memory only.

    InMemDiscovery inMemDiscovery = new InMemDiscovery();
    EventBus eventBus = new EventBus();

    this.muon = new MultiTransportMuon(config, inMemDiscovery, Collections.singletonList(new InMemTransport(config, eventBus)), new JsonOnlyCodecs());

    AutoConfiguration configEvStore = MuonConfigBuilder.withServiceIdentifier("eventstore").withTags("eventstore")
            .build();
    Muon evStore = new MultiTransportMuon(configEvStore, inMemDiscovery, Collections.singletonList(new InMemTransport(configEvStore, eventBus)), new JsonOnlyCodecs());

    this.testEventStore = new TestEventStore(evStore);
  }

  void seal(ScriptService scriptService) {
    if (gson==null) {
      gson = createDefaultGson();
    }
    throwIfNotProperlyConfigured();
    this.scriptService = scriptService;
  }

  private Gson createDefaultGson() {
    return new GsonBuilder()
      .registerTypeAdapterFactory(createEventJsonTypeAdapterFactory())
      .disableHtmlEscaping()
      // .setPrettyPrinting()
      .create();
  }

  public void throwIfNotProperlyConfigured() {
    for (Field field: getClass().getDeclaredFields()) {
      if (!OPTIONAL_FIELDS.contains(field.getName())) {
        Object value = null;
        try {
          field.setAccessible(true);
          value = field.get(this);
        } catch (IllegalAccessException e) {
          throw new ScriptException(e);
        }
        ScriptException.throwIfNull(value, "ServiceLocator field '%s' is null", field.getName());
      }
    }
  }

  protected abstract ScriptService createEngine();

  public EngineConfiguration gson(Gson gson) {
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

  public ScriptService getScriptService() {
    return scriptService;
  }
}

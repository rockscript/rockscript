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

public class EngineConfiguration {

  protected EventStore eventStore;
  protected ScriptStore scriptStore;
  protected EventListener eventListener;
  protected IdGenerator scriptIdGenerator;
  protected IdGenerator scriptExecutionIdGenerator;
  protected LockService lockService;
  protected ImportResolver importResolver;

  protected EngineConfiguration() {
  }

  public EngineConfiguration(EventStore eventStore,
                             ScriptStore scriptStore,
                             EventListener eventListener,
                             IdGenerator scriptIdGenerator,
                             IdGenerator scriptExecutionIdGenerator,
                             LockService lockService) {
    this.eventStore = eventStore;
    this.scriptStore = scriptStore;
    this.eventListener = eventListener;
    this.scriptIdGenerator = scriptIdGenerator;
    this.scriptExecutionIdGenerator = scriptExecutionIdGenerator;
    this.lockService = lockService;
  }

  public void throwIfNotProperlyConfigured() {
    for (Field field: getClass().getDeclaredFields()) {
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

  public LockService getLockService() {
    return lockService;
  }

  public ImportResolver getImportResolver() {
    return importResolver;
  }
}

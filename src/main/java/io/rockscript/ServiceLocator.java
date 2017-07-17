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

import java.lang.reflect.Field;

import io.rockscript.engine.*;

public class ServiceLocator {

  protected EventStore eventStore = new EventStore(this);
  protected ScriptStore scriptStore = new ScriptStore();
  protected ImportResolver importResolver = new ImportResolver(this);
  protected EventListener eventListener = eventStore;
  protected ScriptExecutionIdGenerator scriptExecutionIdGenerator;
  protected LockService lockService = new LockServiceImpl();

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

  public ImportResolver getImportResolver() {
    return importResolver;
  }

  public EventListener getEventListener() {
    return eventListener;
  }

  public ScriptExecutionIdGenerator getScriptExecutionIdGenerator() {
    return scriptExecutionIdGenerator;
  }

  public LockService getLockService() {
    return lockService;
  }
}

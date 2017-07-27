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

import com.google.inject.*;

public class ServiceLocator {

  static final Key<IdGenerator> SCRIPT_ID_GENERATOR_KEY = Key.get(IdGenerator.class, IdGenerator.Script.class);
  static final Key<IdGenerator> SCRIPT_EXECUTION_ID_GENERATOR_KEY = Key.get(IdGenerator.class, IdGenerator.ScriptExecution.class);

  @Inject
  Injector serviceBrewery;

  public EventStore getEventStore() {
    return serviceBrewery.getInstance(EventStore.class);
  }

  public ScriptStore getScriptStore() {
    return serviceBrewery.getInstance(ScriptStore.class);
  }

  public ImportResolver getImportResolver() {
    return serviceBrewery.getInstance(ImportResolver.class);
  }

  public EventListener getEventListener() {
    return serviceBrewery.getInstance(EventListener.class);
  }

  public IdGenerator getScriptExecutionIdGenerator() {
    return serviceBrewery.getInstance(SCRIPT_EXECUTION_ID_GENERATOR_KEY);
  }

  public LockService getLockService() {
    return serviceBrewery.getInstance(LockService.class);
  }

  public IdGenerator getScriptIdGenerator() {
    return serviceBrewery.getInstance(SCRIPT_ID_GENERATOR_KEY);
  }
}

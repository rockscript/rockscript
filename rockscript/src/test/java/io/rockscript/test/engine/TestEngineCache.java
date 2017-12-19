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
package io.rockscript.test.engine;

import io.rockscript.Engine;
import io.rockscript.test.TestEngine;

import java.util.HashMap;
import java.util.Map;

public class TestEngineCache {

  protected Engine previousTestEngine = null;
  protected Map<Class<? extends TestEngineProvider>,Engine> cachedEngines = new HashMap<>();

  public Engine getTestEngine(TestEngineProvider engineProvider) {
    Class<? extends TestEngineProvider> providerClass = engineProvider.getClass();
    Engine engine = cachedEngines.get(providerClass);
    if (previousTestEngine!=null && previousTestEngine!=engine) {
      previousTestEngine.stop();
    }
    if (engine==null) {
      engine = engineProvider.createEngine();
      cachedEngines.put(providerClass, engine);
    }
    return engine;
  }
}

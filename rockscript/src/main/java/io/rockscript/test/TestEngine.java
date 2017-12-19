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
package io.rockscript.test;

import io.rockscript.Engine;
import io.rockscript.engine.job.JobService;
import io.rockscript.util.Maps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;

/** Engine with predictable execution of async stuff because
 * it's all executed directly in the thread of the client. */
public class TestEngine extends Engine {

  public TestEngine() {
    this(null);
  }

  public TestEngine(Map<String,String> configuration) {
    super(initializeConfiguration(configuration));
  }

  private static Map<String, String> initializeConfiguration(Map<String, String> configuration) {
    if (configuration==null) {
      configuration = new HashMap<>();
    }
    configuration.put(CFG_KEY_ENGINE, CFG_VALUE_ENGINE_TEST);
    return configuration;
  }

  @Override
  public TestEngine start() {
    return (TestEngine) super.start();
  }

  @Override
  public TestJobService getJobService() {
    return (TestJobService) super.getJobService();
  }
}

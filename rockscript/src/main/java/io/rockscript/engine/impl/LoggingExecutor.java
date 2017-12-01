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
package io.rockscript.engine.impl;

import java.util.concurrent.Executor;

/** Wraps all commands that are executed so that if an exception
 * happens comes out of the commands, it is logged in the
 * {@link EngineLogStore} */
public class LoggingExecutor implements Executor {

  EngineLogStore engineLogStore;
  Executor executor;

  public LoggingExecutor(EngineLogStore engineLogStore, Executor executor) {
    this.engineLogStore = engineLogStore;
    this.executor = executor;
  }

  public static class LoggingRunnable implements Runnable {
    Runnable command;
    EngineLogStore engineLogStore;
    public LoggingRunnable(Runnable command, EngineLogStore engineLogStore) {
      this.command = command;
      this.engineLogStore = engineLogStore;
    }
    @Override
    public void run() {
      try {
        command.run();
      } catch (Throwable exception) {
        engineLogStore.error("Couldn't execute async command "+command+": "+ exception.getMessage(), exception);
      }
    }
  }

  @Override
  public void execute(Runnable command) {
    executor.execute(new LoggingRunnable(command, engineLogStore));
  }
}

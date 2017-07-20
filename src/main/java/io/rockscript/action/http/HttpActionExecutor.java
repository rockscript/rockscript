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
package io.rockscript.action.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.rockscript.Engine;
import io.rockscript.action.*;

/**
 * A work queue that queues HTTP action invocations in an unbounded queue and executes them using a single thread.
 */
class HttpActionExecutor {

  private static final int NUMBER_OF_THREADS = 1;
  ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

  Engine engine;

  public HttpActionExecutor(Engine engine) {
    this.engine = engine;
  }

  /**
   * Queues input that the executor will use to execute the HTTP action.
   */
  public void addActionInput(ActionInput actionInput) {
    executor.execute(new Worker(actionInput));
  }

  private void actionDone(ActionOutput actionOutput) {
    // TODO engine.endWaitingExecutionId needs to be refactored
    // to actionDone and take an ActionOutput as an argument
    engine.endWaitingAction(
        actionOutput.scriptExecutionId,
        actionOutput.executionId,
        actionOutput.result);
  }

  /**
   * Executes the action.
   */
  private class Worker implements Runnable {

    private final ActionInput input;
    private Action action = new HttpAction();

    Worker(ActionInput input) {
      this.input = input;
    }

    @Override
    public void run() {
      ActionResponse response = action.invoke(null, input.args);
      actionDone(new ActionOutput(input, response));
    }
  }
}
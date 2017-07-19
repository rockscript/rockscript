package io.rockscript.action.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.rockscript.Engine;
import io.rockscript.action.*;
import io.rockscript.engine.ActionResponse;

/**
 * Queues HTTP action invocations in an unbounded queue and executes them using a single thread.
 */
class HttpActionWorkQueue {

  private static final int NUMBER_OF_THREADS = 1;
  ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

  Engine engine;

  public HttpActionWorkQueue(Engine engine) {
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
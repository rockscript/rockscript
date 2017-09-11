package io.rockscript.engine.impl;

import io.rockscript.engine.ScriptExecutionException;

public class ErrorExecutionEvent extends ExecutionEvent {

  String error;

  /** constructor for gson deserialization */
  ErrorExecutionEvent() {
  }

  public ErrorExecutionEvent(ScriptExecutionException e) {
    this.error = e.getMessage();
  }

  public String getError() {
    return error;
  }
}

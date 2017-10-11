package io.rockscript.engine.impl;

import java.time.Instant;

public class ActivityStartErrorEvent extends ScriptExecutionErrorEvent<ArgumentsExpressionExecution> {

  Instant retryTime;

  /** constructor for gson deserialization
   * */
  ActivityStartErrorEvent() {
  }

  public ActivityStartErrorEvent(ArgumentsExpressionExecution execution, String error, Instant retryTime) {
    super(execution, error);
    this.retryTime = retryTime;
  }

  public Instant getRetryTime() {
    return retryTime;
  }

  @Override
  public void execute(ArgumentsExpressionExecution execution) {
    execution.failedAttemptsCount++;
  }

  @Override
  public String toString() {
    return "[" + scriptExecutionId + "|" + executionId + "] " +
           "Activity error [script:"+scriptId+",line:"+line+"] "+error+(retryTime!=null ? ", retry scheduled for "+retryTime.toString() : "");
  }
}

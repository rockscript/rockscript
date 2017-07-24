package io.rockscript.engine;

public class ScriptExecutionContext {

  public final String scriptExecutionId;
  public final String executionId;

  public ScriptExecutionContext(String scriptExecutionId, String executionId) {
    this.scriptExecutionId = scriptExecutionId;
    this.executionId = executionId;
  }
}

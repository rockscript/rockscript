package io.rockscript.engine;

public class ScriptExecutionContext {

  public final String scriptExecutionId;

  /**
   * The current execution position within the script execution.
   * This execution position  has to be provided in case the invocation is asynchronous when calling back
   * the completion of this function with {@link ScriptExecution#endFunctionInvocationExecution(String, Object)}
   */
  public final String executionId;

  public ScriptExecutionContext(String scriptExecutionId, String executionId) {
    this.scriptExecutionId = scriptExecutionId;
    this.executionId = executionId;
  }
}

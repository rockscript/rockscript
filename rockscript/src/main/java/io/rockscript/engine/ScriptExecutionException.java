package io.rockscript.engine;

import io.rockscript.activity.ActivityInput;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.engine.impl.Execution;

public class ScriptExecutionException extends EngineException {

  EngineScriptExecution scriptExecution;

  public ScriptExecutionException(EngineScriptExecution scriptExecution, String message) {
    super(message);
    this.scriptExecution = scriptExecution;
  }

  public ScriptExecutionException(EngineScriptExecution scriptExecution, Throwable cause) {
    super(cause);
    this.scriptExecution = scriptExecution;
  }

  public ScriptExecutionException(EngineScriptExecution scriptExecution, String message, Throwable cause) {
    super(message, cause);
    this.scriptExecution = scriptExecution;
  }

  public ScriptExecutionException(EngineScriptExecution scriptExecution, String message, ActivityInput activityInput) {
    super(message, activityInput);
    this.scriptExecution = scriptExecution;
  }

  public ScriptExecutionException(EngineScriptExecution scriptExecution, String message, Execution execution) {
    super(message, execution);
    this.scriptExecution = scriptExecution;
  }

  public EngineScriptExecution getScriptExecution() {
    return scriptExecution;
  }
}

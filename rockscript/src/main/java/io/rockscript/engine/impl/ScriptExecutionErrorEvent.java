package io.rockscript.engine.impl;

public class ScriptExecutionErrorEvent<T extends Execution> extends ExecutableEvent<T> {

  String error;
  String scriptId;

  /** constructor for gson deserialization */
  ScriptExecutionErrorEvent() {
  }

  @Override
  public void execute(T execution) {
  }

  public ScriptExecutionErrorEvent(T execution, String error) {
    super(execution);
    this.scriptId = execution.getEngineScript().getScript().getId();
    this.error = error;
  }

  public String getError() {
    return error;
  }

  @Override
  public String toString() {
    return "[" + scriptExecutionId +"] " +
           "Error [script:"+scriptId+",line:"+line+"] "+error;
  }

  public String getScriptId() {
    return scriptId;
  }
}

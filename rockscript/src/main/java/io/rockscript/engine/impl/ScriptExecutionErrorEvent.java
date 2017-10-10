package io.rockscript.engine.impl;

public class ScriptExecutionErrorEvent extends ExecutionEvent<Execution> {

  String error;
  String scriptId;

  /** constructor for gson deserialization */
  ScriptExecutionErrorEvent() {
  }

  public ScriptExecutionErrorEvent(Execution execution, String error) {
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

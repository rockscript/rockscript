package io.rockscript.action;

import java.util.List;

public class ActionInput {

  public final String scriptExecutionId;
  public final String executionId;
  public final List<Object> args;

  // TODO Combine scriptExecutionId and executionId into a ScriptContext parameter.
  public ActionInput(String scriptExecutionId, String executionId, List<Object> args) {
    this.scriptExecutionId = scriptExecutionId;
    this.executionId = executionId;
    this.args = args;
  }
}

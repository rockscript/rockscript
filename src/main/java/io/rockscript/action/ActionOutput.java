package io.rockscript.action;

public class ActionOutput {

  public final String scriptExecutionId;
  public final String executionId;
  public final Object result;

  public ActionOutput(ActionInput actionInput, Object result) {
    scriptExecutionId = actionInput.scriptExecutionId;
    executionId = actionInput.executionId;
    this.result = result;
  }
}

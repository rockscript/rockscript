package io.rockscript.api.commands;

import io.rockscript.api.model.ScriptExecution;
import io.rockscript.api.Response;

import java.util.List;

public class RecoverCrashedScriptExecutionsResponse implements Response {

  public List<ScriptExecution> scriptExecutions;

  public RecoverCrashedScriptExecutionsResponse() {
  }

  public RecoverCrashedScriptExecutionsResponse(List<ScriptExecution> scriptExecutions) {
    this.scriptExecutions = scriptExecutions;
  }

  @Override
  public int getStatus() {
    return 200;
  }

  public List<ScriptExecution> getScriptExecutions() {
    return scriptExecutions;
  }
}

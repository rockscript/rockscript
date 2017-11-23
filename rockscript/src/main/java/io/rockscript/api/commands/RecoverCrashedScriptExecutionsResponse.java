package io.rockscript.api.commands;

import io.rockscript.api.model.ScriptExecution;

import java.util.List;

public class RecoverCrashedScriptExecutionsResponse {

  public List<ScriptExecution> scriptExecutions;

  public RecoverCrashedScriptExecutionsResponse() {
  }

  public RecoverCrashedScriptExecutionsResponse(List<ScriptExecution> scriptExecutions) {
    this.scriptExecutions = scriptExecutions;
  }

  public List<ScriptExecution> getScriptExecutions() {
    return scriptExecutions;
  }
}

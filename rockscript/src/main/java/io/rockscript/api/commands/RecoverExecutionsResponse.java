package io.rockscript.api.commands;

import io.rockscript.api.model.ScriptExecution;

import java.util.List;

public class RecoverExecutionsResponse {

  public List<ScriptExecution> scriptExecutions;

  public RecoverExecutionsResponse() {
  }

  public RecoverExecutionsResponse(List<ScriptExecution> scriptExecutions) {
    this.scriptExecutions = scriptExecutions;
  }

  public List<ScriptExecution> getScriptExecutions() {
    return scriptExecutions;
  }
}

package io.rockscript.api.commands;

import io.rockscript.Engine;
import io.rockscript.api.Command;
import io.rockscript.api.model.ScriptExecution;

import java.util.List;
import java.util.stream.Collectors;

public class RecoverExecutionsCommand implements Command<RecoverExecutionsResponse> {

  @Override
  public String getType() {
    return "recoverExecutions";
  }

  @SuppressWarnings("unchecked")
  @Override
  public RecoverExecutionsResponse execute(Engine engine) {
    List<ScriptExecution> recoveredScriptExecutions = (List) engine
      .getScriptExecutionStore()
      .recoverCrashedScriptExecutions()
      .stream()
      .map(ese->ese.toScriptExecution())
      .collect(Collectors.toList());
    return new RecoverExecutionsResponse(recoveredScriptExecutions);
  }
}

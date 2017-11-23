package io.rockscript.api.commands;

import io.rockscript.Engine;
import io.rockscript.api.model.ScriptExecution;
import io.rockscript.api.Command;

import java.util.List;
import java.util.stream.Collectors;

public class RecoverCrashedScriptExecutionsCommand implements Command<RecoverCrashedScriptExecutionsResponse> {
  @Override
  public RecoverCrashedScriptExecutionsResponse execute(Engine engine) {
    List<ScriptExecution> recoveredScriptExecutions = (List) engine
      .getEventStore()
      .recoverCrashedScriptExecutions()
      .stream()
      .map(ese->ese.toScriptExecution())
      .collect(Collectors.toList());
    return new RecoverCrashedScriptExecutionsResponse(recoveredScriptExecutions);
  }
}

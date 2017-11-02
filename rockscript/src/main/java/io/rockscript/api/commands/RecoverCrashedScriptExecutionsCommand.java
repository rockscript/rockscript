package io.rockscript.api.commands;

import io.rockscript.engine.Configuration;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.api.Command;

import java.util.List;
import java.util.stream.Collectors;

public class RecoverCrashedScriptExecutionsCommand extends Command<RecoverCrashedScriptExecutionsResponse> {
  @Override
  public RecoverCrashedScriptExecutionsResponse execute(Configuration configuration) {
    List<ScriptExecution> recoveredScriptExecutions = (List) configuration
      .getEventStore()
      .recoverCrashedScriptExecutions()
      .stream()
      .map(ese->ese.toScriptExecution())
      .collect(Collectors.toList());
    return new RecoverCrashedScriptExecutionsResponse(recoveredScriptExecutions);
  }
}

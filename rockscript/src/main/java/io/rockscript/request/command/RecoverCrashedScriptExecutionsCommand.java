package io.rockscript.request.command;

import io.rockscript.engine.Configuration;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.request.CommandImpl;

import java.util.List;
import java.util.stream.Collectors;

public class RecoverCrashedScriptExecutionsCommand extends CommandImpl<RecoverCrashedScriptExecutionsResponse> {
  @Override
  protected RecoverCrashedScriptExecutionsResponse execute(Configuration configuration) {
    List<ScriptExecution> recoveredScriptExecutions = (List) configuration
      .getEventStore()
      .recoverCrashedScriptExecutions()
      .stream()
      .map(ese->ese.toScriptExecution())
      .collect(Collectors.toList());
    return new RecoverCrashedScriptExecutionsResponse(recoveredScriptExecutions);
  }
}

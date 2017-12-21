/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript.api.queries;

import io.rockscript.Engine;
import io.rockscript.api.Query;
import io.rockscript.api.events.ExecutionEvent;
import io.rockscript.api.model.ScriptExecution;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.*;
import io.rockscript.http.servlet.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** Query to fetch script execution details */
public class ScriptExecutionQuery implements Query<ScriptExecutionQuery.ScriptExecutionDetails> {

  static Logger log = LoggerFactory.getLogger(ScriptExecutionQuery.class);

  String id;

  @Override
  public String getName() {
    return "script-execution";
  }

  public static class ScriptExecutionDetails extends ScriptExecution {
    List<ExecutionEvent> events;
    ScriptVersion scriptVersion;
    public ScriptExecutionDetails(
      EngineScriptExecution scriptExecution,
      List<ExecutionEvent> events,
      ScriptVersion scriptVersion) {
      super(scriptExecution);
      this.events = events;
      this.scriptVersion = scriptVersion;
    }
    public List<ExecutionEvent> getEvents() {
      return events;
    }
    public ScriptVersion getScriptVersion() {
      return scriptVersion;
    }
  }

  @Override
  public ScriptExecutionDetails execute(Engine engine) {
    BadRequestException.throwIfNull(id, "id is a required parameter");

    ScriptExecutionStore scriptExecutionStore = engine.getScriptExecutionStore();

    EngineScriptExecution engineScriptExecution = scriptExecutionStore
      .findScriptExecutionById(id);

    ScriptVersion scriptVersion = engineScriptExecution
      .getEngineScript()
      .getScriptVersion();

    List<ExecutionEvent> events = scriptExecutionStore.findEventsByScriptExecutionId(id);

    return new ScriptExecutionDetails(
        engineScriptExecution,
        events,
        scriptVersion);
  }
}

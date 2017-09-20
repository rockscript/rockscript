/*
 * Copyright ©2017, RockScript.io. All rights reserved.
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
package io.rockscript.server.handlers;

import com.google.gson.Gson;
import io.rockscript.engine.Configuration;
import io.rockscript.engine.Script;
import io.rockscript.engine.impl.*;
import io.rockscript.netty.router.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Handles GET requests for /scripts */
@Get("/scriptExecutions")
public class ScriptExecutionsHandler implements RequestHandler {

  public static class ScriptExecution {
    String id;
    String scriptName;
    Integer scriptVersion;
    Instant start;
    Instant end;
    public ScriptExecution(){
    }
    public ScriptExecution(String scriptExecutionId) {
      this.id = scriptExecutionId;
    }
  }

  public static class ScriptExecutionList {
    private final Map<String, Script> scriptsById;
    Map<String,ScriptExecution> scriptExecutions = new LinkedHashMap<>();

    public ScriptExecutionList(Map<String, Script> scriptsById) {
      this.scriptsById = scriptsById;
    }

    public void processEvent(Event event) {
      ExecutionEvent executionEvent = (ExecutionEvent) event;
      String scriptExecutionId = executionEvent.getScriptExecutionId();
      ScriptExecution scriptExecution = scriptExecutions
        .computeIfAbsent(scriptExecutionId, se -> new ScriptExecution(scriptExecutionId));

      if (executionEvent instanceof ScriptStartedEvent) {
        scriptExecution.start = executionEvent.getTime();
        ScriptStartedEvent scriptStartedEvent = (ScriptStartedEvent) executionEvent;
        String scriptId = scriptStartedEvent.getScriptId();
        Script script = scriptsById.get(scriptId);
        scriptExecution.scriptName = script.getName();
        scriptExecution.scriptVersion = script.getVersion();

      } else if (executionEvent instanceof ScriptEndedEvent) {
        scriptExecution.end = executionEvent.getTime();
      }
    }
  }

  @Override
  public void handle(Request request, Response response, Context context) {
    Configuration configuration = context.get(Configuration.class);
    Gson gson = configuration.getGson();

    Map<String, Script> scriptsById = new HashMap<>();
    configuration
      .getScriptStore()
      .getScripts()
      .values()
      .forEach(scriptList->scriptList
        .forEach(script->scriptsById.put(script.getId(), script)));

    ScriptExecutionList list = new ScriptExecutionList(scriptsById);
    EventStore eventStore = configuration.getEventStore();
    eventStore
      .getEvents()
      .forEach(list::processEvent);
    response
      .bodyString(gson.toJson(list.scriptExecutions.values()))
      .headerContentTypeApplicationJson()
      .status(200)
      .send();
  }
}

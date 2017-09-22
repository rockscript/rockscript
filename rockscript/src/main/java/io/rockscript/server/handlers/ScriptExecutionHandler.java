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

import io.rockscript.engine.Configuration;
import io.rockscript.engine.impl.ExecutionEvent;
import io.rockscript.engine.impl.ScriptStartedEvent;
import io.rockscript.netty.router.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** Handles GET requests for /scripts */
@Get("/scriptExecution/:scriptExecutionId")
public class ScriptExecutionHandler implements RequestHandler {

  static Logger log = LoggerFactory.getLogger(ScriptExecutionHandler.class);

  public static class ScriptExecution {
    public String id;
    public String scriptText;
    public String scriptName;
    public List<ExecutionEvent> events;
  }

  @Override
  public void handle(Request request, Response response, Context context) {
    String scriptExecutionId = request.getPathParameter("scriptExecutionId");

    Configuration configuration = context.get(Configuration.class);

    ScriptExecution scriptExecution = new ScriptExecution();
    scriptExecution.id = scriptExecutionId;
    scriptExecution.events = configuration
      .getEventStore()
      .findEventsByScriptExecutionId(scriptExecutionId);

    ScriptStartedEvent startEvent = (ScriptStartedEvent) scriptExecution.events.get(0);
    String scriptId = startEvent.getScriptId();
    scriptExecution.scriptText = configuration
      .getScriptStore()
      .findScriptAstById(scriptId)
      .getScript()
      .getText();

    response
      .bodyJson(scriptExecution)
      .headerContentTypeApplicationJson()
      .status(200)
      .send();
  }
}

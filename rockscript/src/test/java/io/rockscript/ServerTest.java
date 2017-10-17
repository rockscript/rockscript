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
package io.rockscript;

import com.google.gson.reflect.TypeToken;
import io.rockscript.engine.*;
import io.rockscript.engine.impl.ContinuationReference;
import io.rockscript.request.command.*;
import io.rockscript.server.handlers.ScriptExecutionHandler;
import io.rockscript.server.handlers.ScriptExecutionsHandler;
import io.rockscript.test.AbstractServerTest;
import io.rockscript.test.SimpleImportProvider;
import io.rockscript.util.Io;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.*;

public class ServerTest extends AbstractServerTest {

  @Override
  public void setUp() {
    super.setUp();
    SimpleImportProvider.setUp();
  }

  @Test
  public void testGetScriptExecutions() {
    String scriptText = null;
    try {
      scriptText = Io.toString(new FileInputStream("src/test/resources/testscripts/short-script.rs"));
    } catch (Exception e) {
      e.printStackTrace();
    }


    EngineDeployScriptResponse deployScriptResponse = newPost("command")
      .bodyObject(new DeployScriptCommand()
        .scriptText(scriptText)
        .scriptName("sn"))
      .execute()
      .assertStatusOk()
      .getBodyAs(EngineDeployScriptResponse.class);

    String scriptId = deployScriptResponse.getId();

    assertNotNull(scriptId);

    EngineStartScriptExecutionResponse startScriptResponse = newPost("command")
      .bodyObject(new StartScriptExecutionCommand()
        .scriptId(scriptId))
      .execute()
      .assertStatusOk()
      .getBodyAs(EngineStartScriptExecutionResponse.class);

    String scriptExecutionId = startScriptResponse.getScriptExecutionId();

    ContinuationReference continuationReference = SimpleImportProvider.removeFirstContinuationReference(scriptExecutionId);

    newPost("command")
      .bodyObject(new EndActivityCommand()
        .continuationReference(continuationReference))
      .execute()
      .assertStatusOk()
      .getBodyAs(EndActivityResponse.class);

    newPost("command")
      .bodyObject(new StartScriptExecutionCommand()
        .scriptId(scriptId))
      .execute()
      .assertStatusOk()
      .getBodyAs(EngineStartScriptExecutionResponse.class);


    List<ScriptExecutionsHandler.ScriptExecution> scriptExecutions = newGet("scriptExecutions")
      .execute()
      .assertStatusOk()
      .getBodyAs(new TypeToken<List<ScriptExecutionsHandler.ScriptExecution>>() {}.getType());

    ScriptExecutionsHandler.ScriptExecution scriptExecution = scriptExecutions.get(0);
    assertEquals("se1", scriptExecution.id);
    assertEquals("sn", scriptExecution.scriptName);
    assertEquals("sn", scriptExecution.scriptShortName);
    assertEquals(1, (int)scriptExecution.scriptVersion);
    assertNotNull(scriptExecution.start);
    assertNotNull(scriptExecution.end);

    scriptExecution = scriptExecutions.get(1);
    assertEquals("se2", scriptExecution.id);
    assertEquals("sn", scriptExecution.scriptName);
    assertEquals("sn", scriptExecution.scriptShortName);
    assertEquals(1, (int)scriptExecution.scriptVersion);
    assertNotNull(scriptExecution.start);
    assertNull(scriptExecution.end);

    Object body = newGet("scriptExecution/se1")
      .execute()
      .assertStatusOk()
      .getBody();

    ScriptExecutionHandler.ScriptExecution scriptExecutionDetails = newGet("scriptExecution/se1")
      .execute()
      .assertStatusOk()
      .getBodyAs(new TypeToken<ScriptExecutionHandler.ScriptExecution>() {}.getType());

    assertTrue(scriptExecutionDetails.scriptText.contains("\n"));
  }

  @Test
  public void testEvents() {
    EngineDeployScriptResponse deployScriptResponse = newPost("command")
      .bodyObject(new DeployScriptCommand()
        .scriptText("var http = system.import('rockscript.io/http'); \n" +
                    "var msg = {hello: 'world'};"))
      .execute()
      .assertStatusOk()
      .getBodyAs(EngineDeployScriptResponse.class);

    String scriptId = deployScriptResponse.getId();

    EngineStartScriptExecutionResponse startScriptResponse = newPost("command")
      .bodyObject(new StartScriptExecutionCommand()
        .scriptId(scriptId))
      .execute()
      .assertStatusOk()
      .getBodyAs(EngineStartScriptExecutionResponse.class);

    String scriptExecutionId = startScriptResponse.getScriptExecutionId();
    EventsResponse eventsResponse = newPost("query")
      .bodyObject(new EventsQuery()
        .scriptExecutionId(scriptExecutionId))
      .execute()
      .assertStatusOk()
      .getBodyAs(EventsResponse.class);

    assertTrue(eventsResponse.getEvents().size()>2);
  }
}
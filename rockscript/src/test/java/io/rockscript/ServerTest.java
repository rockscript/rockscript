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
import io.rockscript.engine.impl.Event;
import io.rockscript.test.AbstractServerTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ServerTest extends AbstractServerTest {

  @Test
  public void testServer() {
    EngineDeployScriptResponse deployScriptResponse = newPost("command")
      .bodyObject(new DeployScriptCommand()
        .scriptText(""))
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
  }

  @Test
  public void testEvents() {
    EngineDeployScriptResponse deployScriptResponse = newPost("command")
      .bodyObject(new DeployScriptCommand()
        .scriptText("var msg = {hello: 'world'};"))
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
    List<Event> events = newGet("events")
      .queryParameterNotNull("seid", scriptExecutionId)
      .execute()
      .assertStatusOk()
      .getBodyAs(new TypeToken<List<Event>>() {
      }.getType());

    assertTrue(events.size()>2);
  }

//  @Test
//  public void testEventSerialization() {
//    Gson gson = server.serviceConfiguration.getGson();
//    EngineScript engineScript = new EngineScript(1, null);
//    Script script = new Script();
//    script.setId("s1");
//    engineScript.setScript(script);
//    EngineScriptExecution engineScriptExecution = new EngineScriptExecution("se1", server.serviceConfiguration, engineScript);
//    Object input = Maps.hashMap(Maps.entry("a", "b"));
//    ScriptStartedEvent event = new ScriptStartedEvent(engineScriptExecution.getScriptExecution(), input);
//    List<Event> eventList = new ArrayList<>();
//    eventList.add(event);
//    log.debug(gson.toJson(eventList));
//  }
}
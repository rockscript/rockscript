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
import io.rockscript.engine.DeployScriptCommand;
import io.rockscript.engine.EngineDeployScriptResponse;
import io.rockscript.engine.StartScriptExecutionCommand;
import io.rockscript.engine.EngineStartScriptExecutionResponse;
import io.rockscript.engine.impl.Event;
import io.rockscript.test.AbstractServerTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    List<Event> eventJsons = newGet("events")
      .execute()
      .assertStatusOk()
      .getBodyAs(new TypeToken<List<Event>>(){}.getType());

    assertTrue(eventJsons.size()>2);
  }

}

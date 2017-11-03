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
package io.rockscript.server;

import com.google.gson.reflect.TypeToken;
import io.rockscript.api.commands.SaveScriptVersionResponse;
import io.rockscript.api.commands.SaveScriptVersionCommand;
import io.rockscript.engine.impl.Event;
import io.rockscript.api.commands.EngineStartScriptExecutionResponse;
import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.test.AbstractServerTest;
import io.rockscript.test.SimpleImportProvider;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class ServerTest extends AbstractServerTest {

  @Override
  public void setUp() {
    super.setUp();
    SimpleImportProvider.setUp();
  }

  @Test
  public void testEvents() {
    SaveScriptVersionResponse saveScriptVersionResponse = newPost("command")
      .bodyObject(new SaveScriptVersionCommand()
        .scriptText("var simple = system.import('rockscript.io/simple'); \n" +
                    "simple.wait();" +
                    "var msg = {hello: 'world'};")
        .activate())
      .execute()
      .assertStatusOk()
      .getBodyAs(SaveScriptVersionResponse.class);

    String scriptId = saveScriptVersionResponse.getId();

    EngineStartScriptExecutionResponse startScriptResponse = newPost("command")
      .bodyObject(new StartScriptExecutionCommand()
        .scriptVersionId(scriptId))
      .execute()
      .assertStatusOk()
      .getBodyAs(EngineStartScriptExecutionResponse.class);

    String scriptExecutionId = startScriptResponse.getScriptExecutionId();
    List<Event> events = newGet("events?scriptExecutionId="+scriptExecutionId)
      .execute()
      .assertStatusOk()
      .getBodyAs(new TypeToken<List<Event>>(){}.getType());

    assertTrue(events.size()>2);
  }
}
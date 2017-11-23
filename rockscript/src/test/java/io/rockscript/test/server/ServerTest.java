/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.rockscript.test.server;

import com.google.gson.reflect.TypeToken;
import io.rockscript.api.commands.*;
import io.rockscript.engine.impl.Event;
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
      .bodyJson(new SaveScriptVersionCommand()
        .scriptText("var simple = system.import('rockscript.io/simple'); \n" +
                    "simple.wait();" +
                    "var msg = {hello: 'world'};")
        .activate())
      .execute(SaveScriptVersionResponse.class)
      .assertStatusOk()
      .getBody();

    String scriptId = saveScriptVersionResponse.getId();

    ScriptExecutionResponse startScriptResponse = newPost("command")
      .bodyJson(new StartScriptExecutionCommand()
        .scriptVersionId(scriptId))
      .execute(ScriptExecutionResponse.class)
      .assertStatusOk()
      .getBody();

    String scriptExecutionId = startScriptResponse.getScriptExecutionId();
    List<Event> events = newGet("query?q=events&scriptExecutionId="+scriptExecutionId)
      .execute(new TypeToken<List<Event>>(){}.getType())
      .assertStatusOk()
      .getBody();

    assertTrue(events.size()>2);
  }
}
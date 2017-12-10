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
import io.rockscript.api.commands.DeployScriptVersionCommand;
import io.rockscript.api.commands.ScriptExecutionResponse;
import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.api.queries.ScriptExecutionQuery;
import io.rockscript.test.SimpleImportProvider;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ServerScenarioTest extends AbstractServerTest {

  @Override
  public void setUp() {
    super.setUp();
    SimpleImportProvider.setUp();
  }

  @Test
  public void testScenario() {
    ScriptVersion saveScriptVersionResponse = newPost("/command")
      .bodyJson(new DeployScriptVersionCommand()
        .scriptText(
          "var simple = system.import('rockscript.io/simple'); \n" +
          "simple.wait();" +
          "var msg = {hello: 'world'};"))
      .execute()
      .assertStatusOk()
      .getBodyAs(ScriptVersion.class);

    String scriptId = saveScriptVersionResponse.getId();

    ScriptExecutionResponse startScriptResponse = newPost("/command")
      .bodyJson(new StartScriptExecutionCommand()
        .scriptVersionId(scriptId))
      .execute()
      .assertStatusOk()
      .getBodyAs(ScriptExecutionResponse.class);

    String scriptExecutionId = startScriptResponse.getScriptExecutionId();
    ScriptExecutionQuery.ScriptExecutionDetails scriptExecutionDetails =
        newGet("/query/script-execution?id=" + scriptExecutionId)
      .execute()
      .assertStatusOk()
      .getBodyAs(new TypeToken<ScriptExecutionQuery.ScriptExecutionDetails>(){}.getType());

    assertTrue(scriptExecutionDetails.getEvents().size()>2);
  }
}
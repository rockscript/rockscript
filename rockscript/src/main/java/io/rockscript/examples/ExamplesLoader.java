/*
 * Copyright (c) 2018 RockScript.io.
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
package io.rockscript.examples;

import io.rockscript.Engine;
import io.rockscript.EngineListener;
import io.rockscript.api.commands.DeployScriptVersionCommand;
import io.rockscript.api.commands.ScriptExecutionResponse;
import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.util.Io;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExamplesLoader implements EngineListener {

  static Logger log = LoggerFactory.getLogger(ExamplesLoader.class);

  @Override
  public void engineStarts(Engine engine) {
    List<ScriptVersion> exampleScripts = new ArrayList<>();
    exampleScripts.add(deployExampleScript(engine, "examples/local-error.rs"));
    exampleScripts.add(deployExampleScript(engine, "examples/local-retry.rs"));
    exampleScripts.add(deployExampleScript(engine, "examples/star-wars.rs"));
    exampleScripts.add(deployExampleScript(engine, "examples/chuck-norris.rs"));

    List<ScriptExecutionResponse> exampleExecutions = new ArrayList<>();
    exampleExecutions.add(startExampleScriptExecution(engine, "examples/star-wars.rs"));
    exampleExecutions.add(startExampleScriptExecution(engine, "examples/chuck-norris.rs"));

    log.debug("Examples initialized: "+exampleScripts.size()+" example scripts and "+exampleExecutions.size()+" script executions available:");
    exampleScripts.forEach(script->log.debug("  Script "+script.getId()+" : "+script.getScriptName()));
    exampleExecutions.forEach(execution->log.debug("  Script execution "+execution.getScriptExecutionId()+" : "+execution.getEngineScriptExecution().getEngineScript().getScriptVersion().getScriptName()));
  }

  private ScriptExecutionResponse startExampleScriptExecution(Engine engine, String scriptName) {
    return new StartScriptExecutionCommand()
      .scriptName(scriptName)
      .execute(engine);
  }

  private ScriptVersion deployExampleScript(Engine engine, String resource) {
    String scriptText = Io.getResourceAsString(resource);
    return new DeployScriptVersionCommand()
      .scriptName(resource)
      .scriptText(scriptText)
      .execute(engine);
  }

  @Override
  public void engineStops(Engine engine) {
  }
}

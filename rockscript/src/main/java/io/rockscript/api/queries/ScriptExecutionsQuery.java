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
package io.rockscript.api.queries;

import com.google.gson.Gson;
import io.rockscript.Engine;
import io.rockscript.api.Query;
import io.rockscript.api.events.ExecutionEvent;
import io.rockscript.api.events.ScriptEndedEvent;
import io.rockscript.api.events.ScriptStartedEvent;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.ScriptExecutionStore;

import java.time.Instant;
import java.util.*;

public class ScriptExecutionsQuery implements Query<Collection<ScriptExecutionsQuery.ScriptExecution>> {

  @Override
  public String getName() {
    return "script-executions";
  }

  public static class ScriptExecution {
    public String id;
    public String scriptShortName;
    public String scriptName;
    public Integer scriptVersion;
    public Instant start;
    public Instant end;
    public ScriptExecution(){
    }
    public ScriptExecution(String scriptExecutionId, List<ExecutionEvent> scriptExecutionEvents, Map<String, ScriptVersion> scriptVersionsById) {
      this.id = scriptExecutionId;

      scriptExecutionEvents.forEach(event->{
        if (event instanceof ScriptStartedEvent) {
          this.start = event.getTime();
          ScriptStartedEvent scriptStartedEvent = (ScriptStartedEvent) event;
          String scriptVersionId = scriptStartedEvent.getScriptVersionId();
          ScriptVersion scriptVersion = scriptVersionsById.get(scriptVersionId);
          this.scriptName = scriptVersion.getScriptName();
          this.scriptShortName = getScriptShortName(scriptVersion.getScriptName());
          this.scriptVersion = scriptVersion.getVersion();

        } else if (event instanceof ScriptEndedEvent) {
          this.end = event.getTime();
        }
      });
    }

    private static String getScriptShortName(String name) {
      int lastSlashIndex = name.lastIndexOf('/');
      if (lastSlashIndex>=0 && name.length()>lastSlashIndex+1) {
        return name.substring(lastSlashIndex+1);
      }
      return name;
    }
  }

  @Override
  public Collection<ScriptExecution> execute(Engine engine) {
    Gson gson = engine.getGson();

    Map<String, ScriptVersion> scriptVersionsById = new HashMap<>();
    engine
      .getScriptStore()
      .getScripts()
      .forEach(script->script
        .getScriptVersions()
        .forEach(scriptVersion->scriptVersionsById.put(scriptVersion.getId(), scriptVersion)));

    List<ScriptExecution> scriptExecutions = new ArrayList<>();

    ScriptExecutionStore scriptExecutionStore = engine.getScriptExecutionStore();
    scriptExecutionStore
      .findAllScriptExecutionIds()
      .forEach(scriptExecutionId->{
        List<ExecutionEvent> scriptExecutionEvents = scriptExecutionStore.findEventsByScriptExecutionId(scriptExecutionId);
        scriptExecutions.add(new ScriptExecution(scriptExecutionId, scriptExecutionEvents, scriptVersionsById));
      });

    return scriptExecutions;
  }
}

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

import io.rockscript.Engine;
import io.rockscript.api.Query;
import io.rockscript.api.model.Script;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.http.servlet.BadRequestException;

import java.util.List;

public class ScriptVersionsQuery implements Query<List<ScriptVersion>> {

  @Override
  public String getType() {
    return "scriptVersions";
  }

  String scriptId;

  @Override
  public List<ScriptVersion> execute(Engine engine) {
    BadRequestException.throwIfNull(scriptId, "Query parameter scriptId is not specified");
    Script script = engine.getScriptStore().getScripts().stream()
      .filter(s -> scriptId.equals(s.getId()))
      .findFirst()
      .orElse(null);
    BadRequestException.throwIfNull(script, "Script %s not found", scriptId);
    return script.getScriptVersions();
  }
}

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

package io.rockscript.engine;

import io.rockscript.Script;
import io.rockscript.service.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptStore {

  Configuration configuration;
  /** maps script name to script versions */
  Map<String, List<Script>> scriptsByName = new HashMap<>();
  /** maps script ids to parsed EngineScript's */
  Map<String, EngineScript> scriptAstsById = new HashMap<>();

  public ScriptStore(Configuration configuration) {
    this.configuration = configuration;
  }

  public EngineScript findScriptAstById(String scriptId) {
    EngineScript engineScript = scriptAstsById.get(scriptId);
    if (engineScript ==null) {
      throw new RuntimeException("TODO finish this");
    }
    return engineScript;
  }

  /** Stores the script, caches the engineScript and
   * assigns the next version to script.version */
  public void deploy(Script script, EngineScript engineScript) {
    String id = script.getId();
    String name = script.getName();
    List<Script> scriptVersions = scriptsByName.get(name);
    if (scriptVersions==null) {
      scriptVersions = new ArrayList<>();
      scriptsByName.put(name, scriptVersions);
    }
    script.setVersion(scriptVersions.size());
    scriptVersions.add(script);

    engineScript.setScript(script);
    engineScript.setConfiguration(configuration);
    scriptAstsById.put(id, engineScript);
  }
}

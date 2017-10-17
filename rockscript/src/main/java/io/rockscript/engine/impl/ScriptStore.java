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

package io.rockscript.engine.impl;

import io.rockscript.engine.Configuration;
import io.rockscript.request.command.EngineDeployScriptResponse;
import io.rockscript.engine.EngineException;
import io.rockscript.engine.Script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ScriptStore {

  Configuration configuration;
  /** maps script name to script versions */
  Map<String, List<Script>> scriptsByName = new HashMap<>();
  /** maps script ids to parsed EngineScript's */
  Map<String, EngineScript> scriptAstsById = new HashMap<>();

  public ScriptStore(Configuration configuration) {
    this.configuration = configuration;
  }

  public ScriptStore(Configuration configuration, ScriptStore other) {
    this.configuration = configuration;
    this.scriptsByName = other.scriptsByName;
  }

  public EngineScript findLatestScriptAstByName(String scriptName) {
    List<Script> scriptVersions = findScriptVersionsByName(scriptName);
    if (scriptVersions!=null) {
      Script latestVersion = scriptVersions.get(scriptVersions.size()-1);
      String scriptId = latestVersion.getId();
      return findScriptAstById(scriptId);
    }
    return null;
  }

  /** only the last part of the name has to match */
  List<Script> findScriptVersionsByName(String scriptName) {
    if (scriptsByName.containsKey(scriptName)) {
      return scriptsByName.get(scriptName);
    }
    List<String> names = new ArrayList<>();
    for (String name: scriptsByName.keySet()) {
      if (name.endsWith(scriptName)) {
        names.add(name);
      }
    }
    if (names.size()==1) {
      return scriptsByName.get(names.get(0));
    } else if (names.size()>1) {
      throw new EngineException("Ambiguous name: "+names);
    }
    return null;
  }

  public EngineScript findScriptAstById(String scriptId) {
    EngineScript engineScript = scriptAstsById.get(scriptId);
    if (engineScript == null) {
      Script script = findScriptById(scriptId);
      if (script==null) {
        throw new EngineException("Script "+scriptId+" does not exist");
      }
      Parse parse = parseScript(script);
      if (!parse.hasErrors()) {
        engineScript = parse.getEngineScript();
        scriptAstsById.put(scriptId, engineScript);
      }
    }
    return engineScript;
  }

  Script findScriptById(String scriptId) {
    if (scriptId!=null && scriptsByName!=null) {
      for (List<Script> scriptVersions: scriptsByName.values()) {
        for (Script scriptVersion: scriptVersions) {
          if (scriptId.equals(scriptVersion.getId())) {
            return scriptVersion;
          }
        }
      }
    }
    return null;
  }

  public EngineDeployScriptResponse deploy(String scriptName, String scriptText) {
    Script script = new Script();
    script.setText(scriptText);

    if (scriptName ==null) {
      scriptName = "Unnamed script";
    }
    script.setName(scriptName);

    Parse parse = parseScript(script);
    if (!parse.hasErrors()) {
      String id = script.getId();
      if (id==null) {
        id = configuration.getScriptIdGenerator().createId();
        script.setId(id);
      }

      EngineScript engineScript = parse.getEngineScript();
      scriptAstsById.put(id, engineScript);

      // storeScript also assigns the version
      storeScript(script);
    }

    return new EngineDeployScriptResponse(script, parse.getErrors());
  }

  /** Parses the script and initializes
   * the engineScript if parse is succesfull. */
  Parse parseScript(Script script) {
    Parse parse = Parse.create(script.getText());
    if (!parse.hasErrors()) {
      EngineScript engineScript = parse.getEngineScript();
      engineScript.setConfiguration(configuration);
      engineScript.setScript(script);
    }
    return parse;
  }

  void storeScript(Script script) {
    String scriptName = script.getName();
    List<Script> scriptVersions = scriptsByName.get(scriptName);
    if (scriptVersions==null) {
      scriptVersions = new ArrayList<>();
      scriptsByName.put(scriptName, scriptVersions);
    }
    script.setVersion(scriptVersions.size()+1);
    scriptVersions.add(script);
  }

  /** Collects the latest version of each script that has a matching name.
   * @param namePatternRegex is a {@link Pattern regex}*/
  public List<Script> findLatestScriptVersionsByNamePattern(String namePatternRegex) {
    List<Script> matchingScripts = new ArrayList<>();
    for (String name: scriptsByName.keySet()) {
      if (Pattern.matches(namePatternRegex, name)) {
        List<Script> scriptVersions = scriptsByName.get(name);
        if (!scriptVersions.isEmpty()) {
          matchingScripts.add(scriptVersions.get(scriptVersions.size()-1));
        }
      }
    }
    return matchingScripts;
  }

  public Map<String, List<Script>> getScripts() {
    return scriptsByName;
  }
}

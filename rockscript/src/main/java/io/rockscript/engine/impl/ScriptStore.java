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

import io.rockscript.Engine;
import io.rockscript.api.events.ScriptEvent;
import io.rockscript.api.events.ScriptVersionSavedEvent;
import io.rockscript.api.model.Script;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.EngineException;
import io.rockscript.http.servlet.BadRequestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ScriptStore {

  Engine engine;

  /** all stored scripts */
  List<Script> scripts = new ArrayList<>();

  /** maps script version ids to parsed EngineScript's */
  public Map<String, EngineScript> parsedScriptAsts = new HashMap<>();

  public ScriptStore(Engine engine) {
    this.engine = engine;
  }

  public ScriptStore(Engine engine, ScriptStore other) {
    this.engine = engine;
    this.scripts = other.scripts;
  }

  /** Finds the first script for which the name ends with the given scriptNameSuffix */
  public Script findScriptByNameEnd(String scriptNameSuffix) {
    if (scriptNameSuffix==null) {
      return null;
    }
    return scripts.stream()
      .filter(script->script.getName()!=null && script.getName().endsWith(scriptNameSuffix))
      .findFirst()
      .orElse(null);
  }

  public Script findScriptByName(String scriptName) {
    if (scriptName==null) {
      return null;
    }
    return scripts.stream()
      .filter(script->scriptName.equals(script.getName()))
      .findFirst()
      .orElse(null);
  }

  public Script findScriptById(String scriptId) {
    if (scriptId==null) {
      return null;
    }
    return scripts.stream()
      .filter(script->scriptId.equals(script.getId()))
      .findFirst()
      .orElse(null);
  }

  public void insertScript(Script script) {
    EngineException.throwIfNull(script.getName(), "Scripts must have a name");
    if (findScriptByName(script.getName())!=null) {
      throw new BadRequestException("Script with name '" + script.getName() + "' already exists");
    }
    if (script.getId()==null) {
      String scriptId = engine.getScriptIdGenerator().createId();
      script.setId(scriptId);
    }
    if (findScriptById(script.getId())!=null) {
      throw new BadRequestException("Script with id '"+script.getId()+"' already exists");
    }
    scripts.add(0, script);
  }

  public void addParsedScriptAstToCache(String scriptVersionId, EngineScript parsedScriptAst) {
    parsedScriptAsts.put(scriptVersionId, parsedScriptAst);
  }

  private ScriptVersion findScriptVersion(Script script, String scriptVersionId) {
    if (scriptVersionId!=null && script!=null && script.getScriptVersions()!=null) {
      return script.getScriptVersions().stream()
        .filter(scriptVersion->scriptVersionId.equals(scriptVersion.getId()))
        .findFirst()
        .orElse(null);
    }
    return null;
  }

  public void addParsedScriptAstToCache(Parse parse, ScriptVersion scriptVersion) {
    EngineScript parsedScriptAst = parse.getEngineScript();
    if (parsedScriptAst!=null) {
      parsedScriptAst.setScriptVersion(scriptVersion);
      parsedScriptAsts.put(scriptVersion.getId(), parsedScriptAst);
    }
  }

  public EngineScript findScriptAstByScriptVersionId(String scriptVersionId) {
    EngineScript engineScript = parsedScriptAsts.get(scriptVersionId);
    if (engineScript == null) {
      ScriptVersion scriptVersion = findScriptVersionById(scriptVersionId);
      if (scriptVersion!=null) {
        Parse parse = engine
          .getScriptParser()
          .parseScriptText(scriptVersion.getText());
        if (!parse.hasErrors()) {
          addParsedScriptAstToCache(parse, scriptVersion);
          engineScript = parse.getEngineScript();
        }
      }
    }
    return engineScript;
  }

  /** Collects the latest version of each scriptVersion that has a matching name.
   * @param namePatternRegex is a {@link Pattern regex}*/
  public List<ScriptVersion> findLatestScriptVersionsByNamePattern(String namePatternRegex) {
    List<ScriptVersion> matchingScriptVersions = new ArrayList<>();
    for (Script script: scripts) {
      if (Pattern.matches(namePatternRegex, script.getName())) {
        List<ScriptVersion> scriptVersions = script.getScriptVersions();
        if (!scriptVersions.isEmpty()) {
          matchingScriptVersions.add(scriptVersions.get(scriptVersions.size() - 1));
        }
      }
    }
    return matchingScriptVersions;
  }

  ScriptVersion findScriptVersionById(String scriptId) {
    if (scriptId!=null && scripts!=null) {
      for (Script script: scripts) {
        for (ScriptVersion scriptVersion: script.getScriptVersions()) {
          if (scriptId.equals(scriptVersion.getId())) {
            return scriptVersion;
          }
        }
      }
    }
    return null;
  }

  public List<Script> getScripts() {
    return scripts;
  }

  public void handle(ScriptEvent event) {
    if (event instanceof ScriptVersionSavedEvent) {
      handleScriptVersionSavedEvent((ScriptVersionSavedEvent) event);
    }
  }

  public void handleScriptVersionSavedEvent(ScriptVersionSavedEvent event) {
    ScriptVersion scriptVersion = event.getScriptVersion();
    Script script = null;

    String scriptId = scriptVersion.getScriptId();
    if (scriptId!=null) {
      script = findScriptById(scriptId);
      BadRequestException.throwIfNull(script, "Script %s does not exist", scriptId);

    } else {
      String scriptName = scriptVersion.getScriptName();
      script = findScriptByName(scriptName);
      if (script==null) {
        script = new Script();
        script.setName(scriptName);
        // insertScript will assign the id of the script
        insertScript(script);
      }
      scriptVersion.setScriptId(script.getId());
    }

    List<ScriptVersion> scriptVersions = script.getScriptVersions();

    if (Boolean.TRUE.equals(scriptVersion.getActive())) {
      ScriptVersion scriptVersionToInactivate = findScriptVersionById(script.getActiveScriptVersionId());
      if (scriptVersionToInactivate!=null) {
        scriptVersionToInactivate.setActive(null);
      }
      script.setActiveScriptVersionId(scriptVersion.getId());

    } else {
      // Find the latest script version id (if there is one)
      ScriptVersion latestVersion = !scriptVersions.isEmpty() ? scriptVersions.get(scriptVersions.size()-1) : null;
      String latestScriptVersionId = latestVersion!=null ? latestVersion.getId() : null;
      // If the latest version was not the active version
      if (latestScriptVersionId!=null && !latestScriptVersionId.equals(script.getActiveScriptVersionId())) {
        // Remove the latest version because it becomes irrelevant
        // Only one non-active version should be maintained
        scriptVersions.remove(scriptVersions.size()-1);
      }
    }

    scriptVersions.add(scriptVersion);
    scriptVersion.setVersion(scriptVersions.size());
  }
}

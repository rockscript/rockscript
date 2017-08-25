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

import io.rockscript.service.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptStore {

  Configuration configuration;
  /** maps script name to script versions */
  Map<String, List<Script>> scriptsByName = new HashMap<>();
  /** maps script ids to parsed ScriptAst's */
  Map<String, ScriptAst> scriptAstsById = new HashMap<>();

  public ScriptStore(Configuration configuration) {
    this.configuration = configuration;
  }

  public ScriptAst findScriptAstById(String scriptId) {
    ScriptAst scriptAst = scriptAstsById.get(scriptId);
    if (scriptAst==null) {
      throw new RuntimeException("TODO finish this");
    }
    return scriptAst;
  }

  public Script deployScript(String name, String text) {
    Script script = new Script();
    script.setText(text);

    Parse parse = Parse.create(text);
    if (!parse.hasErrors()) {
      String id = configuration.getScriptIdGenerator().createId();
      script.setId(id);

      if (name==null) {
        name = "Unnamed script";
      }
      script.setName(name);

      List<Script> scriptVersions = scriptsByName.get(name);
      if (scriptVersions==null) {
        scriptVersions = new ArrayList<>();
        scriptsByName.put(name, scriptVersions);
      }
      script.setVersion(scriptVersions.size());
      scriptVersions.add(script);

      ScriptAst scriptAst = parse.getScriptAst();
      scriptAst.setId(id);
      scriptAst.setConfiguration(configuration);
      scriptAstsById.put(id, scriptAst);

    } else {
      script.setErrors(parse.getErrors());
    }
    return script;
  }
}

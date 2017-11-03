/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
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
package io.rockscript.api.model;

import java.util.ArrayList;
import java.util.List;

/** A script is a named and versioned RockScript text that can be executed by the engine.
 *
 * RockScript requires scripts to be stored in the engine because it
 * long running scripts may need to be reloaded after long waits.
 *
 * The name can represent eg the path on the file system or any other user defined name.
 * The script name must be unique.
 *
 * A script has many versions.  Max one version can be the active version.  That is
 * the version that will be started when this script is started.  When saving a script,
 * there is an optional 'activate' boolean property to indicate if you want this new
 * version to be the active version.  Saving and activating in one operation is also
 * referred to as deploying a script.
 *
 * When running tests, the active script version id is ignored and the very
 * latest script version is started.
 */
public class Script {

  String id;
  String name;
  List<ScriptVersion> scriptVersions = new ArrayList<>();
  String activeScriptVersionId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ScriptVersion> getScriptVersions() {
    return scriptVersions;
  }

  public void setScriptVersions(List<ScriptVersion> scriptVersions) {
    this.scriptVersions = scriptVersions;
  }

  public String getActiveScriptVersionId() {
    return activeScriptVersionId;
  }

  public void setActiveScriptVersionId(String activeScriptVersionId) {
    this.activeScriptVersionId = activeScriptVersionId;
  }
}

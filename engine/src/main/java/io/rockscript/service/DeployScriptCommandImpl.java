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
package io.rockscript.service;

import io.rockscript.DeployScriptCommand;
import io.rockscript.engine.*;

public class DeployScriptCommandImpl extends CommandImpl<Script> implements DeployScriptCommand {

  protected String name;
  protected String text;

  /** used by gson deserialization */
  DeployScriptCommandImpl() {
  }

  public DeployScriptCommandImpl(Configuration configuration) {
    super(configuration);
  }

  @Override
  protected Script execute(Configuration configuration) {
    ScriptStore scriptStore = configuration.getScriptStore();
    return scriptStore.deployScript(name, text);
  }

  public String getText() {
    return this.text;
  }
  public void setText(String text) {
    this.text = text;
  }
  public DeployScriptCommandImpl text(String text) {
    this.text = text;
    return this;
  }

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public DeployScriptCommandImpl name(String name) {
    this.name = name;
    return this;
  }
}

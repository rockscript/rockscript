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

import java.util.List;
import java.util.stream.Collectors;

/** ScriptVersion data as exposed in the CommandExecutorService API.
 * ScriptVersion's are serializable with Gson. */
public class ScriptVersion {

  protected String id;
  protected String scriptId;
  protected String name;
  protected Integer version;
  protected String text;
  protected List<ParseError> errors;

  public ScriptVersion() {
  }

  public ScriptVersion(ScriptVersion other) {
    if (other!=null) {
      this.id = other.id;
      this.scriptId = other.scriptId;
      this.name = other.name;
      this.text = other.text;
      this.version = other.version;
      this.errors = other.errors;
    }
  }

  public String getId() {
    return id;
  }

  public String getScriptId() {
    return scriptId;
  }

  public void setScriptId(String scriptId) {
    this.scriptId = scriptId;
  }

  public String getName() {
    return name;
  }

  public String getText() {
    return text;
  }

  public Integer getVersion() {
    return version;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public List<ParseError> getErrors() {
    return errors;
  }

  public void setErrors(List<ParseError> errors) {
    this.errors = errors;
  }

  public boolean hasErrors() {
    return errors!=null && !errors.isEmpty();
  }

  public ScriptVersion throwIfErrors() {
    if (hasErrors()) {
      String errorPerLine = errors.stream()
        .map(e->e.toString())
        .collect(Collectors.joining("\n"));
      throw new RuntimeException("Deploy errors: \n" + errorPerLine);
    }
    return this;
  }
}

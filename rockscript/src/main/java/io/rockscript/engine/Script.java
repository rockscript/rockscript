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
package io.rockscript.engine;

/** Script data as exposed in the RequestExecutorService API.
 * Script's are serializable with Gson. */
public class Script {

  protected String id;
  protected String name;
  protected Integer version;
  protected String text;

  public Script() {
  }

  public Script(Script other) {
    this.id = other.id;
    this.name = other.name;
    this.text = other.text;
    this.version = other.version;
  }

  public String getId() {
    return id;
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
}

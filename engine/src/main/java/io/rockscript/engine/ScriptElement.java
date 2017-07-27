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

import java.util.List;


/** Base class for a node in the abstract syntax tree of a {@link Script} */
public abstract class ScriptElement {

  protected Integer index;
  protected Location location;
  protected String text;

  public ScriptElement(Integer index, Location location) {
    this.index = index;
    this.location = location;
  }

  public abstract Execution createExecution(Execution parent);
  /** null or empty list is allowed */
  protected abstract List<? extends ScriptElement> getChildren();

  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public ScriptElementJson toJson() {
    return new ScriptElementJson(this);
  }

  public static class ScriptElementJson {
    Integer index;
    String text;
    Location location;
    public ScriptElementJson(ScriptElement executable) {
      this.index = executable.getIndex();
      this.text = executable.getText();
      this.location = executable.getLocation();
    }
    public ScriptElementJson() {
    }
  }

}

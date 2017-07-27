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
public abstract class Operation {

  protected String id;
  protected Location location;

  public Operation(String id, Location location) {
    this.id = id;
    this.location = location;
  }

  public abstract Execution createExecution(Execution parent);
  /** null or empty list is allowed */
  protected abstract List<? extends Operation> getChildren();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }
}

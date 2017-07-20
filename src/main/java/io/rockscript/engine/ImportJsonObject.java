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
 *
 */
package io.rockscript.engine;

import java.util.List;

import io.rockscript.action.Action;
import io.rockscript.action.ActionResponse;

public class ImportJsonObject extends JsonObject {

  public void resolveActionNames(String url) {
    for (String propertyName: getPropertyNames()) {
      Object value = get(propertyName);
      if (value instanceof Action) {
        put(propertyName, new NamedActionWrapper(url+"/"+propertyName, (Action) value));
      }
    }
  }

  public class NamedActionWrapper implements Action {
    String name;
    Action action;
    public NamedActionWrapper(String name, Action action) {
      this.name = name;
      this.action = action;
    }
    @Override
    public ActionResponse invoke(ArgumentsExpressionExecution argumentsExpressionExecution, List<Object> args) {
      return action.invoke(argumentsExpressionExecution, args);
    }
    public String getName() {
      return name;
    }
    @Override
    public String toString() {
      return "["+name+" action]";
    }
  }

}

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

import java.util.*;

import io.rockscript.action.Action;

public class JsonObject {

  Map<String,Object> fields = new HashMap<>();

  public JsonObject put(String field, Object value) {
    fields.put(field, value);
    return this;
  }

  public JsonObject put(String field, java.util.function.Function<FunctionInput, ActionResponse> functionHandler) {
    fields.put(field, new Action() {
      @Override
      public ActionResponse invoke(ArgumentsExpressionExecution argumentsExpressionExecution, List<Object> args) {
        FunctionInput functionInput = new FunctionInput(argumentsExpressionExecution, args);
        return functionHandler.apply(functionInput);
      }
    });
    return this;
  }

  public Object get(String field) {
    return fields.get(field);
  }


}

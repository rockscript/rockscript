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
package io.rockscript.netty.router;

import java.lang.reflect.Type;

import com.google.gson.Gson;

public class JsonHandlerGson implements JsonHandler {

  Gson gson;

  public JsonHandlerGson(Gson gson) {
    this.gson = gson;
  }

  @Override
  public <T> T fromJsonString(String jsonString, Type type) {
    return gson.fromJson(jsonString, type);
  }

  @Override
  public String toJsonString(Object jsonObject) {
    return gson.toJson(jsonObject);
  }
}

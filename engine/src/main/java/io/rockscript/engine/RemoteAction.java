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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.rockscript.action.Action;
import io.rockscript.action.ActionInput;
import io.rockscript.action.ActionOutput;
import io.rockscript.action.http.HttpRequest;

import static io.rockscript.action.http.Http.ContentTypes.APPLICATION_JSON;
import static io.rockscript.action.http.Http.Headers.CONTENT_TYPE;

public class RemoteAction implements Action {

  String url;
  String actionName;

  public RemoteAction(String url, String actionName) {
    this.url = url;
    this.actionName = actionName;
  }

  @Override
  public ActionOutput invoke(ActionInput input) {
    Gson gson = input.getEngineContext().getGson();
    String actionInputJson = gson.toJson(input);
    Object actionOutputResponse = HttpRequest.createPost(url + "/" + actionName)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .body(actionInputJson)
        .execute()
        .getBody();
    JsonElement actionOutputJsonElement = gson.toJsonTree(actionOutputResponse);
    return gson.fromJson(actionOutputJsonElement, ActionOutput.class);
  }
}

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
package io.rockscript.action.http;

import java.util.concurrent.Executor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.rockscript.ScriptService;
import io.rockscript.action.*;

public class HttpAction implements Action {

  public static final HttpAction GET = new HttpAction(HttpRequest.METHOD_GET);
  public static final HttpAction POST = new HttpAction(HttpRequest.METHOD_POST);

  String method;

  public HttpAction(String method) {
    this.method = method;
  }

  @Override
  public ActionOutput invoke(ActionInput input) {
    // Parse the HttpRequest object
    Object requestObject = input.getArg(0);
    Gson gson = input.getEngineContext().getGson();
    JsonElement requestElement = gson.toJsonTree(requestObject);
    HttpRequest httpRequest = gson.fromJson(requestElement, HttpRequest.class);
    httpRequest.method = this.method;

    // Create the HttpRequestRunnable command
    ScriptService scriptService = input.getEngineContext().getScriptService();
    HttpRequestRunnable command = new HttpRequestRunnable(input.getScriptExecutionId(), input.getExecutionId(), httpRequest, scriptService);

    // Schedule the HttpRequestRunnable command for execution asynchronously
    Executor executor = input.getEngineContext().getExecutor();
    executor.execute(command);

    return ActionOutput.waitForFunctionToCompleteAsync();
  }

  @Override
  public String toString() {
    return method;
  }
}

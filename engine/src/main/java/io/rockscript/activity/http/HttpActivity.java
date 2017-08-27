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
package io.rockscript.activity.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.rockscript.activity.Activity;
import io.rockscript.activity.ActivityInput;
import io.rockscript.activity.ActivityOutput;
import io.rockscript.activity.http.Http.Methods;
import io.rockscript.engine.Engine;

public class HttpActivity implements Activity {

  public static final HttpActivity GET = new HttpActivity(Methods.GET);
  public static final HttpActivity POST = new HttpActivity(Methods.POST);

  String method;

  public HttpActivity(String method) {
    this.method = method;
  }

  @Override
  public ActivityOutput invoke(ActivityInput input) {
    // Parse the HttpRequest object
    Object requestObject = input.getArg(0);
    Gson gson = input.getActivityContext().getGson();
    JsonElement requestElement = gson.toJsonTree(requestObject);
    HttpRequest httpRequest = gson.fromJson(requestElement, HttpRequest.class);
    httpRequest.method = this.method;

    // Create the HttpRequestRunnable command
    Engine engine = input.getActivityContext().getEngine();
    HttpRequestRunnable command = new HttpRequestRunnable(input.getContinuationReference(), httpRequest, engine);

    // Schedule the HttpRequestRunnable command for execution asynchronously
    input
      .getActivityContext()
      .getExecutor()
      .execute(command);

    return ActivityOutput.waitForFunctionToCompleteAsync();
  }

  @Override
  public String toString() {
    return method;
  }
}

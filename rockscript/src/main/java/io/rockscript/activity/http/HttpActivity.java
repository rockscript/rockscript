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
import io.rockscript.activity.AbstractActivity;
import io.rockscript.activity.ActivityInput;
import io.rockscript.activity.ActivityOutput;
import io.rockscript.engine.impl.Engine;
import io.rockscript.http.Http;
import io.rockscript.http.HttpRequest;
import io.rockscript.util.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class HttpActivity extends AbstractActivity {

  public static final HttpActivity GET = new HttpActivity(Http.Methods.GET);
  public static final HttpActivity POST = new HttpActivity(Http.Methods.POST);
  public static final HttpActivity PUT = new HttpActivity(Http.Methods.PUT);
  public static final HttpActivity DELETE = new HttpActivity(Http.Methods.DELETE);

  String method;

  public HttpActivity(String method) {
    super(method.toLowerCase());
    this.method = method;
  }

  @Override
  public List<String> getArgNames() {
    return null;
  }

  @Override
  public ActivityOutput invoke(ActivityInput input) {
    // Parse the HttpRequest object
    Object requestObject = input.getArg(0);

    wrapSingleHeadersInList(requestObject);

    Gson gson = input.getGson();
    JsonElement requestElement = gson.toJsonTree(requestObject);

    HttpRequest httpRequest = gson.fromJson(requestElement, HttpRequest.class);
    httpRequest.setHttp(input.getHttp());
    httpRequest.setMethod(this.method);

    // Create the HttpRequestRunnable command
    Engine engine = input.getEngine();
    HttpRequestRunnable command = new HttpRequestRunnable(input.getContinuationReference(), httpRequest, engine);

    // Schedule the HttpRequestRunnable command for execution asynchronously
    input
      .getExecutor()
      .execute(command);

    return ActivityOutput.waitForEndActivityCallback();
  }

  private void wrapSingleHeadersInList(Object requestObject) {
    if (requestObject instanceof Map) {
      Object headersObject = ((Map<String,Object>)requestObject).get("headers");
      if (headersObject instanceof Map) {
        Map headers = (Map) headersObject;
        for (Object key: headers.keySet()) {
          Object value = headers.get(key);
          if (value!=null &&
               ( !Collection.class.isAssignableFrom(value.getClass())
                 && !value.getClass().isArray())) {
            headers.put(key, Lists.of(value));
          }
        }
      }
    }
  }
}

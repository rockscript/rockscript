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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import io.rockscript.action.Action;
import io.rockscript.action.ActionResponse;
import io.rockscript.engine.ArgumentsExpressionExecution;

public class HttpAction implements Action {

  Request request;

  @Override
  public ActionResponse invoke(ArgumentsExpressionExecution argumentsExpressionExecution, List<Object> args) {
    // TODO Construct the HTTP request from the inputs.
    URL url = null;
    Method method = Method.GET;
    String contentType = null;
    TextRequestBody body = new TextRequestBody(contentType, "");
    request = new Request(url, method, Collections.emptySet(), body);

    // TODO Send the HTTP request using java.net.HttpURLConnection
    try {
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // TODO Construct a Response
      return ActionResponse.endFunction(new Response());
    } catch (IOException e) {
      // TODO Return a failure response
      return ActionResponse.endFunction();
    }
  }
}

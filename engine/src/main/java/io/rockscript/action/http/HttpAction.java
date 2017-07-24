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
import java.net.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import io.rockscript.action.*;

public class HttpAction implements Action {

  private static HttpActionConfiguration configuration = new HttpActionConfiguration();
  static {
    configuration.connectionTimeoutMilliseconds = 0;
    configuration.readTimeoutMilliseconds = 0;
  }

  @Override
  public ActionResponse invoke(ActionInput input) {
    Request request;
    try {
      // TODO Construct the HTTP request from the inputs.
      URL url = new URL("https://api.github.com/orgs/RockScript");
      Method method = Method.GET;
      String contentType = null;
      TextRequestBody body = new TextRequestBody(contentType, null);
      Set<RequestHeader> headers = new HashSet<>();
      headers.add(new RequestHeader("Accept", "application/json"));
      // TODO headers.add("X-Correlation-Id", scriptExecutionId);
      request = new Request(url, method, headers, body);
    } catch (MalformedURLException e) {
      return ActionResponse.endFunction(e);
    }

    try {
      HttpURLConnection connection = new HttpURLConnectionBuilder(configuration, request).build();
      ResponseBodyReader responseBodyReader = new ResponseBodyReader(connection);
      String responseBody = new String(responseBodyReader.read(), Charset.forName("UTF-8"));
      ResponseHeaders headers = new ResponseHeaders(connection.getHeaderFields());
      int status = connection.getResponseCode();
      Response response = new Response(status, connection.getResponseMessage(), responseBody, headers);
      return ActionResponse.endFunction(response);
    } catch (IOException e) {
      return ActionResponse.endFunction(e);
    }
  }
}

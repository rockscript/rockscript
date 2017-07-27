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
import java.nio.charset.Charset;

import com.google.gson.Gson;
import io.rockscript.action.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpAction implements Action {

  private static HttpActionConfiguration configuration = new HttpActionConfiguration();
  static {
    configuration.connectionTimeoutMilliseconds = 0;
    configuration.readTimeoutMilliseconds = 0;
  }

  // Share JSON serialisers between HttpAction instances.
  static Gson gson = new Gson();

  @Override
  public ActionOutput invoke(ActionInput input) {
    Request request;
    try {
      request = new RequestBuilder(input).build();
    } catch (IllegalArgumentException e) {
      return ActionOutput.endFunction(e);
    }

    try {
      request.log();
      HttpURLConnection connection = new HttpURLConnectionBuilder(configuration, request).build();
      ResponseBodyReader responseBodyReader = new ResponseBodyReader(connection);
      String responseBody = new String(responseBodyReader.read(), Charset.forName("UTF-8"));
      ResponseHeaders headers = new ResponseHeaders(connection.getHeaderFields());
      int status = connection.getResponseCode();
      Response response = new Response(status, connection.getResponseMessage(), responseBody, headers);
      response.log();
      return ActionOutput.endFunction(response);
    } catch (IOException e) {
      return ActionOutput.endFunction(e);
    }
  }
}

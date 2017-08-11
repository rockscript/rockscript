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

package io.rockscript.action.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import io.rockscript.ScriptService;
import io.rockscript.util.Io;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestRunnable implements Runnable {

  static Logger log = LoggerFactory.getLogger(HttpRequestRunnable.class);

  String scriptExecutionId;
  String executionId;
  HttpRequest request;
  ScriptService scriptService;

  public HttpRequestRunnable(String scriptExecutionId, String executionId, HttpRequest request, ScriptService scriptService) {
    this.scriptExecutionId = scriptExecutionId;
    this.executionId = executionId;
    this.request = request;
    this.scriptService = scriptService;
  }

  @Override
  public void run() {
    HttpResponse response = execute(request);
    scriptService.endWaitingAction(scriptExecutionId, executionId, response);
  }

  private HttpResponse execute(HttpRequest request) {
    try {
      URL url = new URL(request.getUrl());
      HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
      int responseCode = urlConnection.getResponseCode();
      HttpResponse httpResponse = new HttpResponse(responseCode);
      httpResponse.setHeaders(urlConnection.getHeaderFields());
      InputStream inputStream = urlConnection.getInputStream();

      if (httpResponse.isContentTypeApplicationJson()) {
        Reader bodyReader = new InputStreamReader(inputStream, "UTF-8");
        Object parsedJsonBody = new Gson().fromJson(bodyReader, Object.class);
        httpResponse.setBody(parsedJsonBody);
      } else {
        String stringBody = Io.toString(inputStream);
        httpResponse.setBody(stringBody);
      }
      return httpResponse;

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}

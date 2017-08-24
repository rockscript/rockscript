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

package io.rockscript.activity.http;

import com.google.gson.Gson;
import io.rockscript.activity.http.Http.Methods;
import io.rockscript.util.Io;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequest {
  String method;
  String url;
  Map<String,List<String>> headers;
  String body;

  public HttpRequest() {
  }

  public HttpRequest(String method, String url) {
    this.method = method;
    this.url = url;
  }

  public static HttpRequest createGet(String url) {
    return new HttpRequest(Methods.GET, url);
  }

  public static HttpRequest createPost(String url) {
    return new HttpRequest(Methods.POST, url);
  }

  public HttpResponse execute() {
    try {
      URL url = new URL(this.url);
      HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod(method);

      if (headers!=null) {
        for (String headerName: headers.keySet()) {
          List<String> headerListValue = headers.get(headerName);
          String headerValue = headerListValue
            .stream()
            .collect(Collectors.joining(";"));
          urlConnection.setRequestProperty(headerName, headerValue);
        }
      }

      if (body!=null) {
        urlConnection.setDoOutput(true);
        OutputStream outputStream = urlConnection.getOutputStream();
        outputStream.write(body.getBytes("UTF-8"));
        outputStream.flush();
      }

      int responseCode = urlConnection.getResponseCode();
      HttpResponse httpResponse = new HttpResponse(responseCode);

      httpResponse.setHeaders(urlConnection.getHeaderFields());

      InputStream inputStream = null;
      try {
        inputStream = urlConnection.getInputStream();
      } catch (FileNotFoundException e) {
        inputStream = null;
      }
      if (inputStream!=null) {
        if (httpResponse.isContentTypeApplicationJson()) {
          Reader bodyReader = new InputStreamReader(inputStream, "UTF-8");
          Object parsedJsonBody = new Gson().fromJson(bodyReader, Object.class);
          httpResponse.setBody(parsedJsonBody);
        } else {
          String stringBody = Io.toString(inputStream);
          httpResponse.setBody(stringBody);
        }
      }

      return httpResponse;

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  public HttpRequest header(String name, String value) {
    if (headers==null) {
      headers = new HashMap<>();
    }
    List<String> values = headers.get(name);
    if (values==null) {
      values = new ArrayList<>();
      headers.put(name, values);
    }
    values.add(value);
    return this;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public HttpRequest body(String body) {
    this.body = body;
    return this;
  }
}

/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.rockscript.http.client;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.rockscript.http.client.ClientResponse.getString;

/** AsyncHttpRequest builder */
public class ClientRequest {

  static final String NEWLINE = System.getProperty("line.separator");

  /** transient because this field should not be serialized by gson */
  transient Http http;
  /** transient because this field should not be serialized by gson */
  transient HttpRequestBase apacheRequest;

  protected String method;
  protected String url;
  protected Map<String,List<String>> headers;
  protected String body;

  protected ClientRequest() {
  }

  protected ClientRequest(Http http, String method, String url) {
    this.http = http;
    this.method = method;
    this.url = url;
  }

  /** Executes the request and extracts the string from the
   * response body if there is one. */
  public ClientResponse execute() {
    try {
      if (Http.Methods.GET.equals(method)) {
        this.apacheRequest = new HttpGet(url);
      } else if (Http.Methods.POST.equals(method)) {
        this.apacheRequest = new HttpPost(url);
      } else if (Http.Methods.PUT.equals(method)) {
        this.apacheRequest = new HttpPost(url);
      } else if (Http.Methods.DELETE.equals(method)) {
        this.apacheRequest = new HttpPost(url);
      } else {
        throw new RuntimeException("Invalid HTTP method "+method+" for request "+url);
      }

      if (headers!=null) {
        for (String headerName: headers.keySet()) {
          for (String headerValue: headers.get(headerName)) {
            apacheRequest.addHeader(headerName, headerValue);
          }
        }
      }

      if (body!=null) {
        HttpEntity entity = new StringEntity(body, "UTF-8");
        ((HttpEntityEnclosingRequestBase)apacheRequest).setEntity(entity);
      }

      return createHttpResponse();

    } catch (IOException e) {
      throw new RuntimeException("Couldn't execute request "+url+": "+e.getMessage(), e);
    }
  }

  protected ClientResponse createHttpResponse() throws IOException {
    return new ClientResponse(this);
  }

  @Override
  public String toString() {
    return toString(null);
  }

  public String toString(String prefix) {
    return toString(prefix, Integer.MAX_VALUE);
  }

  public String toString(String prefix, int maxBodyLength) {
    if (prefix==null) {
      prefix = "";
    }
    StringBuilder text = new StringBuilder();
    text.append(prefix);
    text.append("> ");
    text.append(method);
    text.append(" ");
    text.append(url);
    if (headers!=null) {
      for (String headerName: headers.keySet()) {
        List<String> headerListValue = headers.get(headerName);
        String headerValue = headerListValue
          .stream()
          .collect(Collectors.joining("; "));
        text.append(NEWLINE);
        text.append(prefix);
        text.append("  ");
        text.append(headerName);
        text.append(": ");
        text.append(headerValue);
      }
    }
    if (body!=null) {
      text.append(NEWLINE);
      text.append(prefix);
      text.append("  ");
      String bodyCustomized = getString(body, prefix, maxBodyLength);
      text.append(bodyCustomized);
    }
    return text.toString();
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  /** adds a query parameter if the valueUnencoded is not null,
   * both parameterName and valueUnencoded will be encoded using URLEncoder */
  public ClientRequest queryParameterNotNull(String parameterName, String value) {
    if (parameterName!=null && value!=null) {
      if (url.contains("?")) {
        url += "&";
      } else {
        url += "?";
      }
      try {
        url += URLEncoder.encode(parameterName, "UTF-8")+"="+URLEncoder.encode(value, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("Couldn't set parameter");
      }
    }
    return this;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  public ClientRequest header(String name, String value) {
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

  public ClientRequest headerContentType(String contentType) {
    header(Http.Headers.CONTENT_TYPE, contentType);
    return this;
  }

  public ClientRequest headerContentTypeApplicationJson() {
    headerContentType(Http.ContentTypes.APPLICATION_JSON);
    return this;
  }

  public String getBody() {
    return body;
  }

  /** Sets a String as the body for the request
   * with encoding UTF-8 */
  public ClientRequest body(String body) {
    this.body = body;
    return this;
  }

  /** sets Content-Type:application/json and serializes the jsonObject
   * with {@link Http#getGson()} as the body string */
  public ClientRequest bodyJson(Object jsonObject) {
    headerContentTypeApplicationJson();
    String json = http.getGson().toJson(jsonObject);
    return body(json);
  }

  public Http getHttp() {
    return http;
  }

  public void setHttp(Http http) {
    this.http = http;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public HttpRequestBase getApacheRequest() {
    return apacheRequest;
  }
}

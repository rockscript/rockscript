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
package io.rockscript.http;

import io.rockscript.http.Http.ContentTypes;
import io.rockscript.http.Http.Headers;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.rockscript.http.HttpRequest.NEWLINE;


/** Obtain a response by starting from the {@link Http} object
 * use one of the newXxx methods to get a request and then
 * invoke {@link HttpRequest#execute()} */
public class HttpResponse {

  /** transient because this field should not be serialized by gson */
  transient HttpRequest request;
  /** transient because this field should not be serialized by gson */
  transient CloseableHttpResponse apacheResponse;

  protected int status;
  protected Map<String,List<String>> headers;
  protected Object body;

  protected HttpResponse(HttpRequest request) throws IOException {
    this.request = request;
    this.apacheResponse = request.http.apacheHttpClient.execute(request.apacheRequest);
    try {
      this.status = apacheResponse.getStatusLine().getStatusCode();
      this.headers = extractHeaders(apacheResponse);

      HttpEntity entity = apacheResponse.getEntity();
      if (entity != null) {
        this.body = request.getEntityHandler().apply(entity, this);
      }
    } finally {
      apacheResponse.close();
    }
  }

  static Map<String, List<String>> extractHeaders(org.apache.http.HttpResponse apacheResponse) {
    Map<String, List<String>> headers = new LinkedHashMap<>();
    Header[] allHeaders = apacheResponse.getAllHeaders();
    if (allHeaders!=null) {
      for (Header header: allHeaders) {
        headers
          .computeIfAbsent(header.getName(), key -> new ArrayList<>())
          .add(header.getValue());
      }
    }
    return headers;
  }

  @Override
  public String toString() {
    return toString(null);
  }

  public String toString(String prefix) {
    return toString(prefix, null);
  }

  public String toString(String prefix, Integer maxBodyLength) {
    StringBuilder text = new StringBuilder();
    if (prefix==null) {
      prefix = "";
    }
    text.append(prefix);
    text.append("< ");
    text.append(apacheResponse.getStatusLine());
    if (headers!=null) {
      for (String headerName: headers.keySet()) {
        if (headerName!=null) {
          List<String> headerListValue = headers.get(headerName);
          String headerValue = headerListToString(headerListValue);
          text.append(NEWLINE);
          text.append(prefix);
          text.append("  ");
          text.append(headerName);
          text.append(": ");
          text.append(headerValue);
        }
      }
    } else {
      text.append(NEWLINE);
      text.append(prefix);
      text.append("< ");
      text.append(status);
    }
    if (body!=null) {
      text.append(NEWLINE);
      text.append(prefix);
      text.append("  ");

      String body = getBodyAsString();
      if (maxBodyLength!=null && body!=null && body.length()>maxBodyLength) {
        body = body.substring(0, maxBodyLength)+"...";
      }

      text.append(body);
    }
    return text.toString();
  }

  public static String headerListToString(List<String> headerListValue) {
    return headerListValue
        .stream()
        .collect(Collectors.joining(";"));
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public HttpResponse assertStatusOk() {
    return assertStatus(200);
  }

  public HttpResponse assertStatusBadRequest() {
    return assertStatus(400);
  }

  public HttpResponse assertStatusNotFound() {
    return assertStatus(404);
  }

  public HttpResponse assertStatus(int expectedStatus) {
    if (status!=expectedStatus) {
      throw new RuntimeException("Status was "+status+", expected "+expectedStatus);
    }
    return this;
  }

  public Object getBody() {
    return body;
  }

  public String getBodyAsString() {
    if (body instanceof String) {
      return (String) body;
    }
    return body!=null ? body.toString() : null;
  }

  public <T> T getBodyAs(Class<T> clazz) {
    return getBodyAs((Type)clazz);
  }

  public <T> T getBodyAs(Type type) {
    if (body==null) {
      return null;
    }
    if (body instanceof String) {
      return request.http.getCodec().deserialize((String)body, type);
    }
    throw new RuntimeException("Don't know how to convert "+body.getClass().getName()+" to "+type);
  }

  public void setBody(Object body) {
    this.body = body;
  }

  public void setHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  public boolean isContentTypeApplicationJson() {
    return headerContains(Headers.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
  }

  public boolean headerContains(String headerName, String headerValue) {
    if (headers==null) {
      return false;
    }
    List<String> headerValues = headers.get(headerName);
    if (headerValues==null) {
      return false;
    }
    for (String actualValue: headerValues) {
      if (actualValue.contains(headerValue)) {
        return true;
      }
    }
    return false;
  }

  public HttpRequest getRequest() {
    return request;
  }

  public CloseableHttpResponse getApacheResponse() {
    return apacheResponse;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }
}

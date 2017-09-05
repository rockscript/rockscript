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
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpResponse {

  /** transient because this field should not be serialized by gson */
  transient HttpRequest httpRequest;
  /** transient because this field should not be serialized by gson */
  transient org.apache.http.HttpResponse apacheResponse;

  protected int status;
  protected Map<String,List<String>> headers;
  protected Object body;

  public HttpResponse(HttpRequest httpRequest) throws IOException {
    this.httpRequest = httpRequest;
    this.apacheResponse = httpRequest.http.apacheClient.execute(httpRequest.apacheRequest);
    this.status = apacheResponse.getStatusLine().getStatusCode();
    this.headers = extractHeaders(apacheResponse);
    this.body = apacheResponse.getEntity();
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

  public HttpResponse log(String prefix) {
    Http.log.debug(prefix+" < "+apacheResponse.getStatusLine());
    if (headers!=null) {
      for (String headerName: headers.keySet()) {
        if (headerName!=null) {
          List<String> headerListValue = headers.get(headerName);
          String headerValue = headerListToString(headerListValue);
          Http.log.debug(prefix+"     ["+headerName+"] "+ headerValue);
        }
      }
    } else {
      Http.log.debug(prefix+" < "+status);
    }
    if (body!=null) {
      Http.log.debug(prefix+"     "+getBodyAsString());
    }
    return this;
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
    if (body instanceof HttpEntity) {
      try {
        HttpEntity entity = (HttpEntity) body;
        this.body = EntityUtils.toString(entity, "UTF-8");
      } catch (IOException e) {
        throw new RuntimeException("Could not get body as string: "+e.getMessage(), e);
      }
    }
    return (String) this.body;
  }

  public <T> T getBodyAs(Type type) {
    String bodyString = getBodyAsString();
    return httpRequest.http.getCodec().deserialize(bodyString, type);
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
}

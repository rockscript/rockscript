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

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.rockscript.http.client.ClientRequest.NEWLINE;


/** Obtain a response by starting from the {@link Http} object
 * use one of the newXxx methods to get a request and then
 * invoke {@link ClientRequest#execute()} */
public class ClientResponse {

  /** transient because this field should not be serialized by gson */
  transient ClientRequest request;
  /** transient because this field should not be serialized by gson */
  transient CloseableHttpResponse apacheResponse;
  /** transient because this field should not be serialized by gson */
  transient String bodyLog;

  protected int status;
  protected Map<String,List<String>> headers;
  protected Object body;

  protected ClientResponse(ClientRequest request, Type type) throws IOException {
    this.request = request;
    this.apacheResponse = request.http.apacheHttpClient.execute(request.apacheRequest);
    try {
      this.status = apacheResponse.getStatusLine().getStatusCode();
      this.headers = extractHeaders(apacheResponse);

      HttpEntity entity = apacheResponse.getEntity();
      if (entity != null) {
        try {
          if (type!=null) {
            String charset = getContentTypeCharset("UTF-8");
            InputStream content = entity.getContent();
            InputStreamReader reader = new InputStreamReader(content, charset);
            this.body = request.getHttp().getGson().fromJson(reader, type);

            if (Http.log.isDebugEnabled()) {
              // IDEA: Even better would be if we wrapped
              // the content input stream and copying the json
              // as it is read by Gson.   That way, it would be the
              // *exact* contents instead of the reserialized object
              bodyLog = request.getHttp().getGson().toJson(this.body);
            }

          } else {
            this.body = EntityUtils.toString(entity, "UTF-8");
            bodyLog = (String) this.body;
          }
        } catch (Exception e) {
          throw new RuntimeException("Couldn't ready body/entity from http request " + toString());
        }
      }

    } finally {
      if (Http.log.isDebugEnabled()) {
        Http.log.debug("\n"+this.toString());
      }

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

      String bodyString = body.toString();
      if (maxBodyLength!=null && bodyString!=null && bodyString.length()>maxBodyLength) {
        bodyString = bodyString.substring(0, maxBodyLength)+"...";
      }

      if (bodyLog!=null) {
        text.append(bodyLog);
      }
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

  public ClientResponse assertStatusOk() {
    return assertStatus(Http.ResponseCodes.OK_200);
  }

  public ClientResponse assertStatusBadRequest() {
    return assertStatus(Http.ResponseCodes.BAD_REQUEST_400);
  }

  public ClientResponse assertStatusNotFound() {
    return assertStatus(Http.ResponseCodes.NOT_FOUND_404);
  }

  public ClientResponse assertStatus(int expectedStatus) {
    if (status!=expectedStatus) {
      throw new RuntimeException("Status was "+status+", expected "+expectedStatus);
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T getBody() {
    return (T) body;
  }

  public void setHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  public boolean isContentTypeApplicationJson() {
    return headerContains(Http.Headers.CONTENT_TYPE, Http.ContentTypes.APPLICATION_JSON)
      || headerContains(Http.Headers.CONTENT_TYPE, Http.ContentTypes.APPLICATION_LD_JSON);
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

  public ClientRequest getRequest() {
    return request;
  }

  public CloseableHttpResponse getApacheResponse() {
    return apacheResponse;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public List<String> getHeader(String headerName) {
    if (headers!=null) {
      return headers.get(Http.Headers.CONTENT_TYPE);
    }
    return null;
  }

  public String getContentTypeCharset(String defaultCharset) {
    List<String> values = getHeader(Http.Headers.CONTENT_TYPE);
    if (values!=null) {
      for (String value: values) {
        HeaderElement[] headerElements = BasicHeaderValueParser.parseElements(value, (HeaderValueParser) null);
        if (headerElements!=null && headerElements.length>0) {
          NameValuePair charsetPair = headerElements[0].getParameterByName("charset");
          if (charsetPair!=null) {
            return charsetPair.getValue();
          }
        }
      }
    }
    return defaultCharset;
  }
}

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
package io.rockscript.http.servlet;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ServerRequest {

  protected Gson gson;
  protected HttpServletRequest request;
  protected Map<String, String> pathParameters;
  protected String bodyString;
  protected boolean bodyIsReadAsString;

  public ServerRequest(HttpServletRequest request) {
    this.request = request;
  }

  public ServerRequest(HttpServletRequest request, Gson gson) {
    this.request = request;
    this.gson = gson;
  }

  void setPathParameters(Map<String, String> pathParameters) {
    this.pathParameters = pathParameters;
  }

  public String getMethod() {
    return request.getMethod();
  }

  public String getPathInfo() {
    return request.getPathInfo();
  }

  public String getBodyAsString() {
    return getBodyAsString(getCharset());
  }

  private String getCharset() {
    String charset = request.getCharacterEncoding();
    if (charset==null) {
      charset = "UTF-8";
    }
    return charset;
  }

  /** value is read from the input stream the first time
   * and cached for subsequent invocations,
   * Returns null if there is no body. */
  public String getBodyAsString(String charset) {
    if (!bodyIsReadAsString) {
      bodyIsReadAsString = true;
      try {
        request.setCharacterEncoding(charset);
        Scanner scanner = new Scanner(request.getInputStream(), charset).useDelimiter("\\A");
        bodyString = scanner.hasNext() ? scanner.next() : null;
      } catch (IOException e) {
        throw new RuntimeException("Couldn't read request body string: "+e.getMessage(), e);
      }
    }
    return bodyString;
  }

  public <T> T getBodyAs(Type type) {
    return getBodyAs(type, getCharset());
  }

  public <T> T getBodyAs(Type type, String charset) {
    return gson.fromJson(getBodyAsString(charset), type);
  }

  public String getPathParameter(String pathParameterName) {
    return pathParameters.get(pathParameterName);
  }

  /** Returns the value of a request parameter as a <code>String</code>,
   * or <code>null</code> if the parameter does not exist. Request parameters
   * are extra information sent with the request.  For HTTP servlets,
   * parameters are contained in the query string or posted form data.
   *
   * <p>You should only use this method when you are sure the
   * parameter has only one value. If the parameter might have
   * more than one value, use {@link #getQueryParameterMap()}.
   *
   * <p>If you use this method with a multivalued
   * parameter, the value returned is equal to the first value
   * in the array returned by <code>getParameterValues</code>. */
  public String getQueryParameter(String parameterName) {
    return request.getParameter(parameterName);
  }

  /** Returns a java.util.Map of the parameters of this request.
   *
   * <p>Request parameters are extra information sent with the request.
   * For HTTP servlets, parameters are contained in the query string or
   * posted form data.
   *
   * @return an immutable java.util.Map containing parameter names as
   * keys and parameter values as map values. The keys in the parameter
   * map are of type String. The values in the parameter map are of type
   * String array. */
  public Map<String,String[]> getQueryParameterMap() {
    return request.getParameterMap();
  }

  public String toString() {
    return toString(null);
  }

  public String toString(RequestHandler requestHandler) {
    String prefix = "  ";
    return "\n> " + request.getMethod() + " " + request.getPathInfo() +
           getLogHeaders(prefix) +
           getLogBody(requestHandler, prefix);
  }

  private String getLogHeaders(String prefix) {
    if (request.getHeaderNames().hasMoreElements()) {
      return "\n"+ Collections.list(request.getHeaderNames()).stream()
        .map(headerName -> {
          return Collections.list(request.getHeaders(headerName)).stream()
            .map(headerValue -> {
              return prefix + headerName + ": " + headerValue;
            }).collect(Collectors.joining("\n"));
        }).collect(Collectors.joining("\n"));
    } else {
      return "";
    }
  }

  private String getLogBody(RequestHandler requestHandler, String prefix) {
    String logBodyText = null;
    if (requestHandler!=null) {
      logBodyText = requestHandler.getLogBodyText(this);
    } else {
      logBodyText = getBodyAsString();
    }
    return getLogBody(prefix, logBodyText);
  }

  static String getLogBody(String prefix, String logBodyText) {
    if (logBodyText!=null) {
      return "\n"+prefix+logBodyText;
    } else {
      return "";
    }
  }
}

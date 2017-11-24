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
import java.util.Map;
import java.util.Scanner;

public class ServerRequest {

  protected Gson gson;
  protected HttpServletRequest request;
  protected Map<String, String> pathParameters;
  protected String bodyStringUtf8;
  protected boolean bodyIsRead;

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

  public String getBody() {
    return getBody("UTF-8");
  }

    /** value is read from the input stream the first time
     * and cached for subsequent invocations. */
  public String getBody(String charset) {
    if (!bodyIsRead) {
      bodyIsRead = true;
      try {
        request.setCharacterEncoding("UTF-8");
        Scanner scanner = new Scanner(request.getInputStream(), charset).useDelimiter("\\A");
        bodyStringUtf8 = scanner.hasNext() ? scanner.next() : null;
      } catch (IOException e) {
        throw new RuntimeException("Couldn't read request body string: "+e.getMessage(), e);
      }
    }
    return bodyStringUtf8;
  }

  public <T> T getBodyAs(Type type) {
    return getBodyAs(type, "UTF-8");
  }

  public <T> T getBodyAs(Type type, String charset) {
    return gson.fromJson(getBody(charset), type);
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
}

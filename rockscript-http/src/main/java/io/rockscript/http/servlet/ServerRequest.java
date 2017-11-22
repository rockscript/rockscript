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
}

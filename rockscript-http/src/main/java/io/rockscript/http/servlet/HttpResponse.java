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

import io.rockscript.http.client.Http;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

public class HttpResponse {

  HttpServletResponse response;

  public HttpResponse(HttpServletResponse response) {
    this.response = response;
  }

  public void statusOk() {
    status(200);
  }

  public void statusNotFound() {
    status(404);
  }

  public void statusInternalServerError() {
    status(500);
  }

  public void status(int status) {
    this.response.setStatus(status);
  }

  public void bodyString(String responseBody) {
    try {
      ServletOutputStream out = response.getOutputStream();
      byte[] bytes = responseBody.getBytes(Charset.forName("UTF-8"));
      out.write(bytes);
      out.flush();
    } catch (IOException e) {
      throw new RuntimeException("Couldn't send body: "+e.getMessage(), e);
    }
  }

  public void header(String name, String value) {
    if (name!=null && value!=null) {
      response.addHeader(name, value);
    }
  }

  public void headerContentTypeTextPlain() {
    header(Http.Headers.CONTENT_TYPE, Http.ContentTypes.TEXT_PLAIN);
  }

  public void headerContentType(String value) {
    header(Http.Headers.CONTENT_TYPE, value);
  }
}

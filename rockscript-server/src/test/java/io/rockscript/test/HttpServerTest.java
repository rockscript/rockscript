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
package io.rockscript.test;

import io.rockscript.http.server.HttpServer;
import io.rockscript.http.servlet.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HttpServerTest extends AbstractHttpServerTest {

  @Override
  protected void configureServer(HttpServer server) {
    RouterServlet servlet = new RouterServlet();
    servlet.requestHandler(new GreetingHandler());
    servlet.requestHandler(new RuntimeHandler());
    servlet.requestHandler(new BadRequestHandler());
    servlet.exceptionListener(new LatestServerExceptionListener());
    server.servlet(servlet);
  }

  /** All tests in this class that are executed
   * subsequent, will use the same server as configured in
   * {@link #configureServer(HttpServer)}*/
  @Override
  protected String getServerName() {
    return HttpServerTest.class.getName();
  }

  public static class GreetingHandler extends PathRequestHandler {
    protected GreetingHandler() {
      super(GET, "/hello/{greeting}");
    }
    @Override
    public void handle(ServerRequest request, ServerResponse response) {
      response.bodyString("world");
      assertEquals("john", request.getPathParameter("greeting"));
      response.statusOk();
    }
  }

  public static class RuntimeHandler extends PathRequestHandler {
    protected RuntimeHandler() {
      super(GET, "/runtime");
    }
    @Override
    public void handle(ServerRequest request, ServerResponse response) {
      throw new RuntimeException("runtime");
    }
  }

  public static class BadRequestHandler extends PathRequestHandler {
    protected BadRequestHandler() {
      super(GET, "/badrequest");
    }
    @Override
    public void handle(ServerRequest request, ServerResponse response) {
      throw new BadRequestException("badrequest");
    }
  }

  @Test
  public void testHttpOk() {
    String response = newGet("/hello/john")
      .execute()
      .getBody();
    assertEquals("world", response);
  }

  @Test
  public void testServerExceptionCause() {
    try {
      newGet("/badrequest")
        .execute()
        .assertStatusOk();
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertEquals("badrequest", e.getCause().getMessage());
    }
  }

  @Test
  public void testRuntimeException() {
    String body = newGet("/runtime")
      .execute()
      .assertStatusInternalServerException()
      .getBody();
    assertEquals("{\"message\":\"See the server logs for more details\"}", body);
  }

  @Test
  public void testBadRequestException() {
    String body = newGet("/badrequest")
      .execute()
      .assertStatusBadRequest()
      .getBody();
    assertEquals("{\"message\":\"badrequest\"}", body);
  }
}

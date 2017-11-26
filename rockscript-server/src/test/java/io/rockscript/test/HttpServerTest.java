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
import io.rockscript.http.servlet.PathRequestHandler;
import io.rockscript.http.servlet.RouterServlet;
import io.rockscript.http.servlet.ServerRequest;
import io.rockscript.http.servlet.ServerResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HttpServerTest extends AbstractHttpServerTest {

  @Override
  protected void configureServer(HttpServer server) {
    RouterServlet servlet = new RouterServlet();
    servlet.requestHandler(new GreetingHandler());
    servlet.requestHandler(new BangHandler());
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

  public static class BangHandler extends PathRequestHandler {
    protected BangHandler() {
      super(GET, "/bang");
    }
    @Override
    public void handle(ServerRequest request, ServerResponse response) {
      throw new RuntimeException("bang");
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
  public void testHttpInternalServerException() {
    try {
      newGet("/bang")
        .execute()
        .assertStatusOk();
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertEquals("bang", e.getCause().getMessage());
    }
  }
}

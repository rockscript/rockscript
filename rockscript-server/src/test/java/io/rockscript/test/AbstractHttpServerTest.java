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

import io.rockscript.http.Http;
import io.rockscript.http.client.ClientRequest;
import io.rockscript.http.client.ClientResponse;
import io.rockscript.http.client.HttpClient;
import io.rockscript.http.server.HttpServer;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractHttpServerTest {

  protected static Logger log = LoggerFactory.getLogger(AbstractHttpServerTest.class);

  protected static final int PORT = 9999;

  private static HttpClient httpClient;
  private static HttpServer server;
  /** @see #getServerName() */
  private static String serverName;
  private static String baseUrl;

  /** implementions can add servlets and filters to the test server */
  protected abstract HttpServer createHttpServer();
  /** tests with the same serverName can use the cached server */
  protected abstract String getServerName();

  @Before
  public void setUp() {
    if (server!=null) {
      if (!serverName.equals(getServerName())) {
        log.debug("Shutting down server "+serverName);
        server.shutdown();
        server = null;
        serverName = null;
        httpClient = null;
        baseUrl = null;
      }
    }
    if (server==null) {
      this.server = startServer();
      this.serverName = getServerName();
      log.debug("Started server "+serverName);
      this.httpClient = createHttpClient();
      this.baseUrl = createBaseUrl();
    }
  }

  /** override to customize the test server creation */
  protected HttpServer startServer() {
    HttpServer httpServer = createHttpServer();
    httpServer.startup();
    return httpServer;
  }

  /** override to customize the http client */
  protected HttpClient createHttpClient() {
    return new HttpClient();
  }

  /** override to customize the baseUrl */
  protected String createBaseUrl() {
    return "http://localhost:"+server.getPort();
  }

  public String createUri(final String path) {
    return baseUrl+path;
  }

  public ClientRequest newGet(final String path) {
    return new TestClientRequest(httpClient, Http.Methods.GET, createUri(path));
  }

  public ClientRequest newPost(final String path) {
    return new TestClientRequest(httpClient, Http.Methods.POST, createUri(path));
  }

  public ClientRequest newPut(final String path) {
    return new TestClientRequest(httpClient, Http.Methods.PUT, createUri(path));
  }

  public ClientRequest newDelete(final String path) {
    return new TestClientRequest(httpClient, Http.Methods.DELETE, createUri(path));
  }

  static class TestClientRequest extends ClientRequest {
    public TestClientRequest(HttpClient httpClient, String method, String url) {
      super(httpClient, method, url);
    }
    @Override
    protected ClientResponse createHttpResponse() throws IOException {
      return new TestClientResponse(this);
    }
  }

  static class TestClientResponse extends ClientResponse {
    public TestClientResponse(TestClientRequest testHttpRequest) throws IOException {
      super(testHttpRequest);
    }
    @Override
    public ClientResponse assertStatus(int expectedStatus) {
      if (status!=expectedStatus) {
        Throwable serverCause = LatestServerExceptionListener.serverException;
        throw new RuntimeException("Status was " + status + ", expected " + expectedStatus, serverCause);
      }
      return this;
    }
  }
}

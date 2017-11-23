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
package io.rockscript.http.test;

import io.rockscript.http.client.ClientRequest;
import io.rockscript.http.client.ClientResponse;
import io.rockscript.http.client.Http;
import org.junit.AfterClass;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;

public abstract class AbstractHttpServerTest {

  protected static Logger log = LoggerFactory.getLogger(AbstractHttpServerTest.class);

  protected static final int PORT = 9999;

  // http will be cached for all tests in one test class
  private static Http http;
  // server will be cached for all tests in one test class
  private static TestServer server;
  // baseUrl will be cached for all tests in one test class
  private static String baseUrl;

  @Before
  public void setUp() {
    initializeServer();
    initializeHttp();
    initializeBaseUrl();
  }

  private void initializeServer() {
    if (server==null) {
      this.server = createServer();
    }
  }

  /** override to customize the test server creation */
  protected TestServer createServer() {
    TestServer server = new TestServer(PORT);
    server.startup();
    configureServer(server);
    return server;
  }

  /** implementions can add servlets and filters to the test server */
  protected abstract void configureServer(TestServer server);

  private void initializeHttp() {
    if (http==null) {
      this.http = createHttp();
    }
  }

  /** override to customize the http client */
  protected Http createHttp() {
    return new Http();
  }

  private void initializeBaseUrl() {
    if (baseUrl==null) {
      this.baseUrl = createBaseUrl();
    }
  }

  /** override to customize the baseUrl */
  protected String createBaseUrl() {
    return "http://localhost:"+server.getPort()+"/";
  }

  @AfterClass
  public static void tearDownStatic() {
    server.shutdown();
    server = null;
  }

  public String createUri(final String path) {
    return baseUrl+path;
  }

  public ClientRequest newGet(final String path) {
    return new TestClientRequest(http, Http.Methods.GET, createUri(path));
  }

  public ClientRequest newPost(final String path) {
    return new TestClientRequest(http, Http.Methods.POST, createUri(path));
  }

  public ClientRequest newPut(final String path) {
    return new TestClientRequest(http, Http.Methods.PUT, createUri(path));
  }

  public ClientRequest newDelete(final String path) {
    return new TestClientRequest(http, Http.Methods.DELETE, createUri(path));
  }

  static class TestClientRequest extends ClientRequest {
    public TestClientRequest(Http http, String method, String url) {
      super(http, method, url);
    }
    @Override
    protected ClientResponse createHttpResponse(Type type) throws IOException {
      return new TestClientResponse(this, type);
    }
  }

  static class TestClientResponse extends ClientResponse {
    public TestClientResponse(TestClientRequest testHttpRequest, Type type) throws IOException {
      super(testHttpRequest, type);
    }
    @Override
    public ClientResponse assertStatus(int expectedStatus) {
      if (status!=expectedStatus) {
        Throwable serverCause = TestExceptionFilter.serverException;
        throw new RuntimeException("Status was " + status + ", expected " + expectedStatus, serverCause);
      }
      return this;
    }
  }
}

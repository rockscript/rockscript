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
package io.rockscript.test.server;

import io.rockscript.Engine;
import io.rockscript.Servlet;
import io.rockscript.http.client.HttpClient;
import io.rockscript.http.server.HttpServer;
import io.rockscript.test.AbstractHttpServerTest;
import io.rockscript.test.LatestServerExceptionListener;
import io.rockscript.test.engine.TestEngineCache;
import io.rockscript.test.engine.TestEngineProvider;
import org.junit.Before;

import static org.junit.Assert.assertNotNull;

public class AbstractServerTest extends AbstractHttpServerTest {

  protected static TestEngineCache testEngineCache = new TestEngineCache();

  protected Engine engine;

  @Override
  @Before
  public void setUp() {
    engine = testEngineCache.getTestEngine(getEngineProvider());
    // The super.setUp will invoke the createHttpServer below
    super.setUp();
  }

  protected TestEngineProvider getEngineProvider() {
    return TestEngineProvider.DEFAULT_TEST_ENGINE_PROVIDER;
  }

  /** All tests in subclasses of AbstractServerTest that are executed
   * subsequent, will use the same server as configured in
   * {@link AbstractHttpServerTest#createHttpServer()} */
  @Override
  protected String getServerName() {
    // matching this with the engine...
    return getEngineProvider().getClass().getName();
  }

  /** Invoked by super.setUp */
  @Override
  protected HttpServer createHttpServer() {
    // engine is initialized above in the setUp
    assertNotNull(engine);

    Servlet servlet = new Servlet(engine);
    servlet.exceptionListener(new LatestServerExceptionListener());

    return new HttpServer(PORT)
      .servlet(servlet);
  }

  @Override
  protected HttpClient createHttpClient() {
    return new HttpClient(engine.getGson());
  }
}

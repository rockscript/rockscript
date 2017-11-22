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
import io.rockscript.TestEngine;
import io.rockscript.http.test.TestServer;
import org.junit.AfterClass;
import org.junit.Before;

public class AbstractServerTest extends io.rockscript.http.test.AbstractServerTest {

  static Engine engine = new TestEngine().initialize();
  static TestServlet testServlet = new TestServlet(engine);

  /** used to get the test-provided engine used in the {@link Servlet} */
  static class TestServlet extends Servlet {
    Engine engine;
    public TestServlet(Engine engine) {
      this.engine = engine;
    }
    @Override
    protected Engine createEngine() {
      return engine;
    }
  }

  @Override
  @Before
  public void setUp() {
    super.setUp();
    if (engine==null) {
      engine = new TestEngine().initialize();
      testServlet = new TestServlet(engine);
    }
  }

  /** {@link io.rockscript.http.test.AbstractServerTest#tearDownStatic()} will shut down the server */
  @AfterClass
  public static void tearDownStatic() {
    // AbstractServerTest.tearDownStatic will shutdown the server
    engine = null;
    testServlet = null;
  }

  @Override
  protected void configureServer(TestServer server) {
    server.servlet(testServlet);
  }
}

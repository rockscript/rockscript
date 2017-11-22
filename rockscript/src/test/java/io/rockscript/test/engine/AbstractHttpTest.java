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
package io.rockscript.test.engine;

import io.rockscript.http.servlet.RouterServlet;
import io.rockscript.http.test.TestServer;
import org.junit.After;
import org.junit.Before;

/** tests use engine directly and a separate web server is
 * active so that the scripts can reach out to it.
 * Configure the external web server by implementing {@link #configure(RouterServlet)} */
public abstract class AbstractHttpTest extends AbstractEngineTest {

  protected static final int PORT = 4000;
  protected static TestServer testServer;
  protected RouterServlet routerServlet;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    testServer = new TestServer(PORT);
    routerServlet = new RouterServlet();
    configure(routerServlet);
    testServer.servlet(routerServlet);
    testServer.startup();
  }

  @After
  public void tearDown() {
    testServer.shutdown();
    super.tearDown();
  }

  protected abstract void configure(RouterServlet routerServlet);

}

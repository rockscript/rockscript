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
package io.rockscript;

import io.rockscript.http.server.HttpServer;
import io.rockscript.http.servlet.RouterServlet;

public class Server {

  public static final int DEFAULT_ROCKSCRIPT_PORT = 3652;

  private Engine engine;
  private HttpServer server;

  protected void start() {
    this.engine = createEngine();
    this.engine.start();

    this.server = createHttpServer(engine);
    this.server.startup();
  }

  protected HttpServer createHttpServer(Engine engine) {
    RouterServlet servlet = new Servlet(engine)
      .gson(engine.getGson());
    return new HttpServer(DEFAULT_ROCKSCRIPT_PORT)
      .servlet(servlet);
  }

  protected TestEngine createEngine() {
    return new TestEngine();
  }

  protected void join() {
    server.join();
  }

  public static void main(String[] args) {
    runServerTillCtrlC(new Server());
  }

  protected static void runServerTillCtrlC(Server server) {
    server.start();
    server.join();
    System.out.println("RockScript server stopped");
  }
}

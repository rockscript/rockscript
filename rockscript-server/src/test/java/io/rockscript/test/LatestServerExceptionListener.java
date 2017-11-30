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
import io.rockscript.http.servlet.ExceptionListener;
import io.rockscript.http.servlet.ServerRequest;
import io.rockscript.http.servlet.ServerResponse;

import javax.servlet.*;

/** The {@link #serverException} member field will contain the latest
 * server side exception.
 * If a test client request fails or if it does not get the expected response
 * status, the {@link io.rockscript.test.AbstractHttpServerTest.TestClientResponse}
 * uses this serverException as the cause.
 * A {@link LatestServerExceptionListener} is added
 * with {@link io.rockscript.http.servlet.RouterServlet#exceptionListener(ExceptionListener)} */
public class LatestServerExceptionListener implements ExceptionListener {

  public static Throwable serverException = null;

  @Override
  public void exception(ServerRequest request, ServerResponse response, Throwable exception) {
    serverException = exception;
  }
}

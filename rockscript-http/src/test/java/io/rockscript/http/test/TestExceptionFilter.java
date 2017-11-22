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

import javax.servlet.*;
import java.io.IOException;

/** The {@link #serverException} member field will contain the latest
 * server side exception typically used by TestServer.
 * If a test request fails or if it does not get the expected response
 * status, this Http client uses this serverException as the cause.
 * A {@link TestExceptionFilter} is added to {@link TestServer}
 * in it's constructor with {@link TestServer#filter(Filter)} */
public class TestExceptionFilter implements Filter {

  public static Exception serverException = null;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    try {
      serverException = null;
      filterChain.doFilter(servletRequest, servletResponse);
    } catch (IOException | ServletException | RuntimeException e) {
      serverException = e;
      throw e;
    }
  }
}

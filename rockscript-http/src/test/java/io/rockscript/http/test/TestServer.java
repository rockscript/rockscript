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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.net.BindException;

public class TestServer {

  private int port;
  private Server server;
  private ServletHandler servletHandler;

  public TestServer(int port) {
    this.port = port;
    this.server = new Server(port);
    this.servletHandler = new ServletHandler();
    server.setHandler(servletHandler);
    filter(new TestExceptionFilter());
  }

  public TestServer servlet(Class<? extends HttpServlet> servletClass) {
    return servlet(servletClass, "/*");
  }

  public TestServer servlet(Class<? extends HttpServlet> servletClass, String path) {
    servletHandler.addServletWithMapping(servletClass, path);
    return this;
  }

  public TestServer filter(Filter filter) {
    return filter(filter, "/*");
  }

  public TestServer filter(Filter filter, String path) {
    FilterMapping filterMapping = new FilterMapping();
    filterMapping.setFilterName(filter.getClass().getName());
    filterMapping.setPathSpec(path);
    FilterHolder filterHolder = new FilterHolder(filter);
    filterHolder.setName(filter.getClass().getName());
    servletHandler.addFilter(filterHolder, filterMapping);
    return this;
  }

  public void startup() {
    try {
      server.start();
    } catch (Exception e) {
      if (isPortTakenException(e)) {
        // IDEA consider sending a shutdown command.  But only if you can do it safe so that it's impossible to shutdown production servers.
        throw new RuntimeException("Port "+getPort()+" blocked.  You probably have a separate RockScript server running.  Please shut down that one and retry.");
      } else {
        throw new RuntimeException("Couldn't start server: "+ e.getMessage(), e);
      }
    }
  }

  private static boolean isPortTakenException(Throwable t) {
    return "Address already in use".equals(t.getMessage())
           && (t instanceof BindException);
  }

  public void shutdown() {
    try {
      server.stop();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int getPort() {
    return port;
  }
}

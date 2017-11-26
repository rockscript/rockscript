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
package io.rockscript.http.servlet;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/** Usage: Create a subclass, and configure it by
 * overriding {@link HttpServlet#init(ServletConfig)}
 * and invoking {@link #requestHandler(RequestHandler)},
 * and {@link #defaultResponseHeader(String, String)}. */
public class RouterServlet extends HttpServlet {

  static Logger log = LoggerFactory.getLogger(RouterServlet.class);

  /** maps methods to list of request paths */
  private List<RequestHandler> requestHandlers = new ArrayList<>();
  private Map<String,List<String>> defaultResponseHeaders;
  protected Gson gson;

  @Override
  protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
    ServerRequest request = new ServerRequest(servletRequest, gson);
    ServerResponse response = new ServerResponse(servletResponse, gson, servletRequest.getProtocol());

    Optional<RequestHandler> matchingRequestHandler = requestHandlers.stream()
      .filter(requestHandler -> requestHandler.matches(request))
      .findFirst();
    if (matchingRequestHandler.isPresent()) {
      RequestHandler requestHandler = matchingRequestHandler.get();
      try {
        if (log.isDebugEnabled()) log.debug(request.toString(requestHandler));
        applyDefaultResponseHeaders(response);
        requestHandler.handle(request, response);
      } catch (HttpException e) {
        response.status(e.getStatusCode());
        if (log.isDebugEnabled()) log.debug(response.toString());
        throw e;
      }
    } else {
      log.debug("No handler found for "+request.getPathInfo());
      response.statusNotFound();
    }
    if (log.isDebugEnabled()) {
      log.debug(response.toString());
    }
  }

  public RouterServlet requestHandler(RequestHandler requestHandler) {
    requestHandlers.add(requestHandler);
    return this;
  }

  /** default response headers are added after the request handler has
   * returned without throwing exceptions */
  public RouterServlet defaultResponseHeader(String name, String value) {
    if (defaultResponseHeaders==null) {
      defaultResponseHeaders = new LinkedHashMap<>();
    }
    List<String> values = defaultResponseHeaders
      .computeIfAbsent(name, key->new ArrayList<String>());
    values.add(value);
    return this;
  }

  public Map<String,List<String>> getDefaultResponseHeaders() {
    return defaultResponseHeaders;
  }

  private void applyDefaultResponseHeaders(ServerResponse response) {
    if (defaultResponseHeaders!=null) {
      defaultResponseHeaders.forEach((name,values)->{
        if (values!=null) {
          values.forEach(value->{
            response.header(name, value);
          });
        }
      });
    }
  }

  public Gson getGson() {
    return gson;
  }

  public void setGson(Gson gson) {
    this.gson = gson;
  }

  public RouterServlet gson(Gson gson) {
    this.gson = gson;
    return this;
  }
}

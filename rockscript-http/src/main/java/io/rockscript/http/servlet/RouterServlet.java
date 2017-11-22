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

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/** Usage: Create a subclass, and configure it by
 * overriding {@link HttpServlet#init(ServletConfig)}
 * and invoking {@link #requestHandler(RequestHandler)},
 * {@link #requestHandler(String, String, RequestHandler)}
 * and {@link #defaultResponseHeader(String, String)}. */
public class RouterServlet extends HttpServlet {

  /** maps methods to list of request paths */
  private Map<String,List<Path>> pathsByMethod = new HashMap<>();
  private Map<String,List<String>> defaultResponseHeaders;

  @Override
  protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
    HttpRequest request = new HttpRequest(servletRequest);
    HttpResponse response = new HttpResponse(servletResponse);
    List<Path> requestPaths = pathsByMethod.get(request.getMethod());
    if (requestPaths==null) {
      response.statusNotFound();
    } else {
      Optional<Path> matchingPath = requestPaths.stream()
        .filter(path -> path.matches(request))
        .findFirst();
      if (matchingPath.isPresent()) {
        RequestHandler requestHandler = matchingPath.get().getRequestHandler();
        try {
          requestHandler.handle(request, response);
          applyDefaultResponseHeaders(response);
        } catch (HttpException e) {
          response.status(e.getStatusCode());
          throw e;
        }
      } else {
        response.statusNotFound();
      }
    }
  }

  public RouterServlet requestHandler(RequestHandler requestHandler) {
    Class<? extends RequestHandler> requestHandlerClass = requestHandler.getClass();
    try {
      Get get = requestHandlerClass.getAnnotation(Get.class);
      if (get!=null) {
        requestHandler("GET", get.value(), requestHandler);
      }
      Post post = requestHandlerClass.getAnnotation(Post.class);
      if (post!=null) {
        requestHandler("POST", post.value(), requestHandler);
      }
      Delete delete = requestHandlerClass.getAnnotation(Delete.class);
      if (delete!=null) {
        requestHandler("DELETE", delete.value(), requestHandler);
      }
      Put put = requestHandlerClass.getAnnotation(Put.class);
      if (put!=null) {
        requestHandler("PUT", put.value(), requestHandler);
      }
    } catch (Exception e) {
      throw new RuntimeException("Couldn't scan request handler "+requestHandlerClass+": "+e.getMessage(), e);
    }
    return this;
  }

  public RouterServlet requestHandler(
    String method,
    String pathTemplate,
    RequestHandler requestHandler) {
    List<Path> paths = pathsByMethod.get(method);
    if (paths==null) {
      paths = new ArrayList<>();
      pathsByMethod.put(method, paths);
    }
    paths.add(new Path(pathTemplate, requestHandler));
    return this;
  }

  /** default response headers are added after the request handler has
   * returned without throwing exceptions */
  public RouterServlet defaultResponseHeader(String name, String value) {
    if (defaultResponseHeaders==null) {
      defaultResponseHeaders = new LinkedHashMap<>();
    }
    List<String> values = defaultResponseHeaders.get(name);
    if (value==null) {
      values = new ArrayList<>();
      defaultResponseHeaders.put(name, values);
    }
    values.add(value);
    return this;
  }

  public Map<String,List<String>> getDefaultResponseHeaders() {
    return defaultResponseHeaders;
  }

  private void applyDefaultResponseHeaders(HttpResponse response) {
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
}

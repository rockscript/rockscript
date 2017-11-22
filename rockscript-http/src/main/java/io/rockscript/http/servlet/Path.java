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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Path {

  List<PathPart> pathParts;
  RequestHandler requestHandler;

  public Path(String pathTemplate, RequestHandler requestHandler) {
    this.pathParts = PathPart.parse(pathTemplate);
    this.requestHandler = requestHandler;
  }

  /** returns true if this request path matches the given request.
   * If the request path matches, this method also sets the request path
   * parameters on the request. */
  public boolean matches(HttpRequest request) {
    String[] actualParts = request.getPathInfo().split("/");
    if (pathParts.size()==actualParts.length) {
      Map<String,String> pathParameters = new HashMap<>();
      for (int i=0; i<pathParts.size(); i++) {
        PathPart pathPart = pathParts.get(i);
        if (!pathPart.matches(actualParts[i], pathParameters)) {
          return false;
        }
      }
      request.setPathParameters(pathParameters);
      return true;
    }
    return false;
  }

  public RequestHandler getRequestHandler() {
    return requestHandler;
  }
}

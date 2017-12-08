/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript.test;

import io.rockscript.util.Io;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class HttpTestServer {

  static Logger log = LoggerFactory.getLogger(HttpTestServer.class);

  private static final String GET = "GET";
  private static final String POST = "POST";
  private static final String PUT = "PUT";
  private static final String DELETE = "DELETE";

  Server server;
  List<RequestHandler> requestHandlers;

  public HttpTestServer(int port) {
    this.server = new Server(port);
    this.server.setHandler(new TestHandler());
  }

  public HttpTestServer get(String path, BiConsumer<HttpTestRequest, HttpTestResponse> handler) {
    addHandler(GET, path, handler);
    return this;
  }

  public HttpTestServer post(String path, BiConsumer<HttpTestRequest, HttpTestResponse> handler) {
    addHandler(POST, path, handler);
    return this;
  }

  private void addHandler(String method, String path, BiConsumer<HttpTestRequest, HttpTestResponse> handler) {
    requestHandlers.add(new RequestHandler(method, path, handler));
  }

  public void start() {
    try {
      server.start();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't start server", e);
    }
  }
  public void stop() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't stop server", e);
    }
  }

  /** Cleans the request handlers so that a new test can configure and use the same HttpTestServer. */
  public void reset() {
    requestHandlers = new ArrayList<>();
  }

  private class TestHandler extends AbstractHandler {
    @Override
    public void handle(String path, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
      PathMatch pathMatch = findPathMatch(path, request, httpServletRequest, httpServletResponse);
      pathMatch.handler.accept(new HttpTestRequest(httpServletRequest, pathMatch.pathParameters), new HttpTestResponse(httpServletResponse));
    }
    private PathMatch findPathMatch(String path, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
      for (RequestHandler requestHandler: requestHandlers) {
        Map<String,Object> pathParameters = requestHandler.matches(path, request, httpServletRequest, httpServletResponse);
        if (pathParameters!=null) {
          return new PathMatch(requestHandler.handler, pathParameters);
        }
      }
      throw new RuntimeException("No handler found for "+request.getMethod()+" "+path);
    }
  }

  private class PathMatch {
    BiConsumer<HttpTestRequest, HttpTestResponse> handler;
    Map<String,Object> pathParameters;
    public PathMatch(BiConsumer<HttpTestRequest, HttpTestResponse> handler, Map<String, Object> pathParameters) {
      this.handler = handler;
      this.pathParameters = pathParameters;
    }
  }

  private class RequestHandler {
    String method;
    List<RequestPart> requestParts = new ArrayList<>();
    BiConsumer<HttpTestRequest, HttpTestResponse> handler;

    public RequestHandler(String method, String path, BiConsumer<HttpTestRequest, HttpTestResponse> handler) {
      this.method = method;
      this.handler = handler;
      for (String part: splitPath(path)) {
        if (part.startsWith(":")) {
          requestParts.add(new VariablePart(part.substring(1)));
        } else {
          requestParts.add(new FixedPart(part));
        }
      }
    }
    public Map<String, Object> matches(String path, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
      if (!httpServletRequest.getMethod().equals(this.method)) {
        return null;
      }
      List<String> parts = splitPath(path);
      if (this.requestParts.size()!=parts.size()) {
        return null;
      }
      Map<String,Object> pathParameters = new HashMap<>();
      for (int i=0; i<requestParts.size(); i++) {
        RequestPart requestPart = requestParts.get(i);
        String part = parts.get(i);
        if (!requestPart.matches(part, pathParameters)) {
          return null;
        }
      }
      return pathParameters;
    }
    private List<String> splitPath(String path) {
      List<String> parts = new ArrayList<>();
      for (String part: path.split("/")) {
        if (!"".equals(part)) {
          parts.add(part);
        }
      }
      return parts;
    }
  }

  private interface RequestPart {
    boolean matches(String part, Map<String, Object> pathParameters);
  }

  private class FixedPart implements RequestPart {
    String partText;
    public FixedPart(String partText) {
      this.partText = partText;
    }
    @Override
    public boolean matches(String part, Map<String, Object> pathParameters) {
      return partText.equals(part);
    }
  }

  private class VariablePart implements RequestPart {
    String variableName;
    public VariablePart(String variableName) {
      this.variableName = variableName;
    }
    @Override
    public boolean matches(String part, Map<String, Object> pathParameters) {
      pathParameters.put(variableName, part);
      return true;
    }
  }

  public static class HttpTestRequest {
    HttpServletRequest request;
    Map<String,Object> pathParameters;
    public HttpTestRequest(HttpServletRequest request, Map<String,Object> pathParameters) {
      this.request = request;
      this.pathParameters = pathParameters;
    }
    public Object getPathParameter(String parameterName) {
      return pathParameters.get(parameterName);
    }
    public String body() {
      try {
        return Io.getString(request.getInputStream());
      } catch (IOException e) {
        throw new RuntimeException("Couldn't get http request input stream", e);
      }
    }
  }

  public static class HttpTestResponse {
    HttpServletResponse response;
    ServletOutputStream outputStream;
    public HttpTestResponse(HttpServletResponse response) {
      this.response = response;
      try {
        this.outputStream = response.getOutputStream();
      } catch (IOException e) {
        throw new RuntimeException("Couldn't get response output stream", e);
      }
    }
    public HttpTestResponse status(int status) {
      response.setStatus(status);
      return this;
    }
    public HttpTestResponse header(String name, String value) {
      response.addHeader(name, value);
      return this;
    }
    public HttpTestResponse headerContentType(String value) {
      response.addHeader("Content-Type", value);
      return this;
    }
    public HttpTestResponse headerContentTypeApplicationJson() {
      headerContentType("application/json");
      return this;
    }
    public HttpTestResponse body(String body) {
      try {
        log.debug("Writing String to output stream: "+body);
        byte[] bytes = body.getBytes(Charset.forName("UTF-8"));
        log.debug("Writing "+bytes.length+" bytes to output stream "+System.identityHashCode(outputStream));
        outputStream.write(bytes);
      } catch (IOException e) {
        throw new RuntimeException("Couldn't write to response output stream", e);
      }
      return this;
    }

    public void send() {
      try {
        log.debug("Flushing & closing outputstream "+System.identityHashCode(outputStream));
        outputStream.flush();
        outputStream.close();
      } catch (IOException e) {
        throw new RuntimeException("Couldn't flush response output stream", e);
      }
    }
  }
}

/*
 * Copyright ©2017, RockScript.io. All rights reserved.
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

package io.rockscript.netty.router;

import java.nio.charset.Charset;
import java.util.List;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.router.RouteResult;
import org.slf4j.Logger;

import static io.netty.util.CharsetUtil.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

public class Request {

  static final Logger log = getLogger(Request.class);

  Server server;
  FullHttpRequest fullHttpRequest;
  RouteResult<?> route;

  public Request(Server server, FullHttpRequest fullHttpRequest, RouteResult<?> route) {
    this.server = server;
    this.fullHttpRequest = fullHttpRequest;
    this.route = route;
  }

  public String getParameter(String name) {
    return route.param(name);
  }

  public List<String> getParameters(String name) {
    return route.params(name);
  }

  public String getPathParameter(String name) {
    return route.pathParams().get(name);
  }

  public String getQueryParameter(String name) {
    return route.queryParam(name);
  }

  public List<String> getQueryParameters(String name) {
    return route.queryParams().get(name);
  }

  public <T> T getBodyJson(Class<T> type) {
    String jsonBodyString = getBodyStringUtf8();
    return server.getJsonHandler().fromJsonString(jsonBodyString, type);
  }

  public String getBodyStringUtf8() {
    return getBodyString(UTF_8);
  }

  public String getBodyString(Charset charset) {
    String content = fullHttpRequest.content().toString(charset);
    log.debug(">>> "+content);
    return content;
  }

  public boolean isDecodingFailed() {
    return fullHttpRequest.getDecoderResult().isFailure();
  }

  public String getHeader(String name) {
    HttpHeaders headers = fullHttpRequest.headers();
    return headers.get(name);
  }

  public Server getServer() {
    return server;
  }

  public FullHttpRequest getFullHttpRequest() {
    return fullHttpRequest;
  }

  public RouteResult<?> getRoute() {
    return route;
  }
}

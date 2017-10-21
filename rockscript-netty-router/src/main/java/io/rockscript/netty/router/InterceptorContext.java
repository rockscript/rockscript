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

import java.util.List;

public class InterceptorContext {

  int index = -1;
  List<Interceptor> interceptors;
  RequestHandler requestHandler;
  AsyncHttpRequest request;
  AsyncHttpResponse response;
  AsyncHttpServer asyncHttpServer;

  public InterceptorContext(List<Interceptor> interceptors, RequestHandler requestHandler, AsyncHttpRequest request, AsyncHttpResponse response, AsyncHttpServer asyncHttpServer) {
    this.interceptors = interceptors;
    this.requestHandler = requestHandler;
    this.request = request;
    this.response = response;
    this.asyncHttpServer = asyncHttpServer;
  }

  public void next() {
    index++;
    if (index<interceptors.size()) {
      Interceptor interceptor = interceptors.get(index);
      interceptor.intercept(this);
    } else if (index==interceptors.size()) {
      requestHandler.handle(request, response, asyncHttpServer.getContext());
    }
  }

  public AsyncHttpRequest getRequest() {
    return request;
  }

  public AsyncHttpResponse getResponse() {
    return response;
  }

  public AsyncHttpServer getAsyncHttpServer() {
    return asyncHttpServer;
  }

  public RequestHandler getRequestHandler() {
    return requestHandler;
  }
}

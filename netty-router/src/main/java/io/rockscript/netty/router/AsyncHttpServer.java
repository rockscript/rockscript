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

import com.google.gson.JsonObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.router.RouteResult;
import io.netty.handler.codec.http.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncHttpServer {

  static Logger log = LoggerFactory.getLogger(AsyncHttpServer.class);

  protected Router<Class<?>> router;
  protected int port;
  protected Context context;
  protected JsonHandler jsonHandler;
  protected List<Interceptor> interceptors;

  protected NioEventLoopGroup bossGroup;
  protected NioEventLoopGroup workerGroup;
  protected Channel channel;

  public AsyncHttpServer(AsyncHttpServerConfiguration asyncHttpServerConfiguration) {
    this.router = asyncHttpServerConfiguration.getRouter();
    this.port = asyncHttpServerConfiguration.getPort();
    this.context = asyncHttpServerConfiguration.getContext();
    this.interceptors = asyncHttpServerConfiguration.getInterceptors();
    this.jsonHandler = asyncHttpServerConfiguration.getJsonHandler();
  }

  public AsyncHttpServer startup() {
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();

    try {
      ServerBootstrap serverBootstrap = new ServerBootstrap()
        .group(bossGroup, workerGroup);
      serverBootstrap
        .childOption(ChannelOption.TCP_NODELAY, java.lang.Boolean.TRUE)
        .childOption(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.TRUE)
        .channel(NioServerSocketChannel.class)
        // .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(createServerChannelInitializer());

      channel = serverBootstrap
        .bind("localhost", port)
        .sync()
        .channel();
      
      log.debug("AsyncHttpServer started: http://127.0.0.1:" + port + "/\n" + router);

    } catch (Throwable t) {
      t.printStackTrace();
    }
    
    return this;
  }

  public void handleRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
    RouteResult<Class<?>> route = router.route(fullHttpRequest.getMethod(), fullHttpRequest.getUri());
    BadRequestException.checkNotNull(route, "No route for %s %s", fullHttpRequest.getMethod(), fullHttpRequest.getUri());
    
    Request request = new Request(this, fullHttpRequest, route);
    Response response = new Response(this, fullHttpRequest, ctx);

    try {
      Request.log.debug(">>> "+fullHttpRequest.getMethod()+" "+fullHttpRequest.getUri());
      Class<?> requestHandlerClass = route.target();
      
      RequestHandler requestHandler = instantiate(requestHandlerClass);
      if (interceptors!=null) {
        InterceptorContext intercepterContext = new InterceptorContext(interceptors, requestHandler, request, response, this);
        intercepterContext.next();

      } else {
        requestHandler.handle(request, response, context);
      }
      

    } catch (RuntimeException e) {
      if (e instanceof BadRequestException) {
        response.statusBadRequest();
      } else {
        response.statusInternalServerError();
      }
      JsonObject body = new JsonObject();
      body.addProperty("message", e.getMessage());
      response.bodyJson(body);
      response.contentTypeApplicationJson();
      response.send();
      requestException(e, ctx);
    } finally {
      // ?
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T instantiate(Class<?> clazz) {
    try {
      return (T) clazz.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate "+clazz+" : " + e.getMessage(), e);
    }
  }
  
  protected AsyncHttpServerChannelInitializer createServerChannelInitializer() {
    return new AsyncHttpServerChannelInitializer(this);
  }
  
  public void waitForShutdown() {
    try {
      channel.closeFuture().sync();
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      shutdown();
    }
  }

  public void shutdown() {
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
  }

  public void handleDecoderFailure(Throwable t, ChannelHandlerContext ctx) {
    log.error("Request decoding failed: "+t.getMessage(), t);
    t.printStackTrace();
  }
  
  public void requestException(Throwable t, ChannelHandlerContext ctx) {
    log.error("Request exception: "+t.getMessage(), t);
  }

  public Context getContext() {
    return context;
  }

  public Router<Class< ? >> getRouter() {
    return router;
  }
  
  public int getPort() {
    return port;
  }
  
  public List<Interceptor> getInterceptors() {
    return interceptors;
  }

  public JsonHandler getJsonHandler() {
    return jsonHandler;
  }
}
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

import com.google.inject.Injector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.router.RouteResult;
import io.netty.handler.codec.http.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

  static Logger log = LoggerFactory.getLogger(Server.class);

  protected Router<Class<?>> router;
  protected int port;
  protected Injector services;
  protected JsonHandler jsonHandler;
  protected List<Interceptor> interceptors;

  protected NioEventLoopGroup bossGroup;
  protected NioEventLoopGroup workerGroup;
  protected Channel channel;
  protected Context context;

  public Server(ServerConfiguration serverConfiguration) {
    this.router = serverConfiguration.getRouter();
    this.port = serverConfiguration.getPort();
    this.services = serverConfiguration.getServices();
    this.interceptors = serverConfiguration.getInterceptors();

    // The jsonHandler could be configured and fetched from the services (Injector/guice container)
    // but for now I skipped that to limit the amount of configuration that has to create
    // a container.  For now there is only one implementation.
    this.jsonHandler = new JsonHandlerGson(services);
  }

  public Server startup() {
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
      
      log.debug("Server started: http://127.0.0.1:" + port + "/\n" + router);

    } catch (Throwable t) {
      t.printStackTrace();
    }
    
    return this;
  }

  public void handleRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
    RouteResult<Class<?>> route = router.route(fullHttpRequest.getMethod(), fullHttpRequest.getUri());
    BadRequestException.checkNotNull(route, "No route for %s %s", fullHttpRequest.getMethod(), fullHttpRequest.getUri());
    
    Request request = new Request(this, fullHttpRequest, route);
    Response response = new Response(this, ctx);

    try {
      System.err.println();
      Request.log.debug(">>> "+fullHttpRequest.getMethod()+" "+fullHttpRequest.getUri());
      Class<?> requestHandlerClass = route.target();
      
      RequestHandler requestHandler = createRequestHandler(requestHandlerClass, request, response);
      if (interceptors!=null) {
        InterceptorContext intercepterContext = new InterceptorContext(interceptors, requestHandler, request, response, this);
        intercepterContext.next();

      } else {
        requestHandler.handle();
      }
      

    } catch (RuntimeException e) {
      if (e instanceof BadRequestException) {
        response.statusBadRequest();
      } else {
        response.statusInternalServerError();
      }
      response.bodyString("{ \"message\": \"oops\" }");
      response.headerContentTypeApplicationJson();
      requestException(e, ctx);
    } finally {
      HttpResponse httpResponse = response.getHttpResponse();
      if (!HttpHeaders.isKeepAlive(fullHttpRequest)) {
        ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
      } else {
        HttpHeaders headers = fullHttpRequest.headers();
        headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        ctx.writeAndFlush(httpResponse);
      }
      // when adding the ctx.close() it seems that this causes
      // the connection to be closed on the server end, causing
      // exceptions on the client for subsequent requests.
      // ctx.close();
    }      
  }

  public RequestHandler createRequestHandler(Class<?> requestHandlerClass, Request request, Response response) {
    RequestHandler requestHandler = instantiate(requestHandlerClass);
    requestHandler.request = request;
    requestHandler.response = response;
    services.injectMembers(requestHandler);
    return requestHandler;
  }

  public <T> T instantiate(Class<?> clazz) {
    // if a Guice IoC container (services) is configured,
    if (services!=null) {
      // Use the services to instantiate the request handler.
      // This will inject services from the container into the
      // request handler.
      return (T) services.getInstance(clazz);
    }
    // If no services object is configured, we just
    // use plain java reflection to instantiate the object
    try {
      return (T) clazz.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate "+clazz+" : " + e.getMessage(), e);
    }
  }
  
  protected ServerChannelInitializer createServerChannelInitializer() {
    return new ServerChannelInitializer(this);
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
    t.printStackTrace();
  }

  public Context getContext() {
    if (context==null) {
      context = new Context();
    }
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

  public Injector getServices() {
    return services;
  }

  public JsonHandler getJsonHandler() {
    return jsonHandler;
  }
}
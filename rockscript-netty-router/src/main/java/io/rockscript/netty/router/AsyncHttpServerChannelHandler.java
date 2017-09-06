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

import io.netty.channel.*;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
public class AsyncHttpServerChannelHandler extends SimpleChannelInboundHandler<Object> {
  
  private static final Logger log = LoggerFactory.getLogger(AsyncHttpServerChannelHandler.class);
  
  private AsyncHttpServer asyncHttpServer;

  public AsyncHttpServerChannelHandler(AsyncHttpServer asyncHttpServer) {
    this.asyncHttpServer = asyncHttpServer;
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
    if (msg instanceof FullHttpRequest) {
      FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;

      if (HttpHeaders.is100ContinueExpected(fullHttpRequest)) {
        send100Continue(ctx);
      }

      DecoderResult decoderResult = fullHttpRequest.getDecoderResult();
      if (decoderResult.isFailure()) {
        Throwable cause = decoderResult.cause();
        asyncHttpServer.handleDecoderFailure(cause, ctx);
      } else {
        asyncHttpServer.handleRequest(fullHttpRequest, ctx);
      }
    }
  }

  protected static void send100Continue(ChannelHandlerContext ctx) {
    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
    ctx.write(response);
  }


  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    asyncHttpServer.requestException(cause, ctx);
  }
}

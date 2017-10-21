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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;

import java.nio.charset.Charset;

import static io.netty.buffer.Unpooled.buffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

/** Wrapper around the Netty router response API */
public class AsyncHttpResponse {

  private static final Logger log = getLogger(AsyncHttpResponse.class);

  protected AsyncHttpServer asyncHttpServer;
  protected ChannelHandlerContext channelHandlerContext;
  protected HttpVersion httpVersion = HTTP_1_1;
  protected ByteBuf byteBuf = buffer();
  protected HttpResponseStatus status = OK;
  protected HttpHeaders headers = new DefaultHttpHeaders();
  protected String contentStringForLog;
  protected FullHttpRequest fullHttpRequest;

  public AsyncHttpResponse(AsyncHttpServer asyncHttpServer, FullHttpRequest fullHttpRequest, ChannelHandlerContext channelHandlerContext) {
    this.asyncHttpServer = asyncHttpServer;
    this.fullHttpRequest = fullHttpRequest;
    this.channelHandlerContext = channelHandlerContext;
  }

  public AsyncHttpResponse statusOk() {
    return status(OK);
  }

  public AsyncHttpResponse statusNotFound() {
    return status(NOT_FOUND);
  }

  public AsyncHttpResponse statusBadRequest() {
    return status(BAD_REQUEST);
  }

  public AsyncHttpResponse statusInternalServerError() {
    return status(INTERNAL_SERVER_ERROR);
  }

  public AsyncHttpResponse statusCreated() {
    return status(CREATED);
  }

  public AsyncHttpResponse statusNoContent() {
    return status(NO_CONTENT);
  }

  public AsyncHttpResponse status(int status) {
    return status(HttpResponseStatus.valueOf(status));
  }

  public AsyncHttpResponse status(HttpResponseStatus status) {
    this.status = status;
    return this;
  }

  public AsyncHttpResponse bodyJson(Object jsonBodyObject) {
    String jsonBodyString = asyncHttpServer.getJsonHandler().toJsonString(jsonBodyObject);
    bodyString(jsonBodyString);
    headerContentTypeApplicationJson();
    return this;
  }

  public AsyncHttpResponse bodyString(String bodyString) {
    return bodyString(bodyString, UTF_8);
  }

  public AsyncHttpResponse bodyString(String bodyString, Charset charset) {
    if (bodyString!=null) {
      contentStringForLog = bodyString; // the log is produced in getHttpResponse() below
      byteBuf.writeBytes(bodyString.getBytes(charset));
    }
    return this;
  }

  public void headers(HttpHeaders httpHeaders) {
    headers.add(httpHeaders);
  }

  public AsyncHttpResponse header(String name, String value) {
    headers.add(name, value);
    return this;
  }

  public AsyncHttpResponse headerContentType(String contentType) {
    header(CONTENT_TYPE, contentType);
    return this;
  }

  public AsyncHttpResponse headerContentTypeApplicationJson() {
    headerContentType("application/json");
    return this;
  }

  public AsyncHttpResponse headerContentTypeTextPlain() {
    headerContentType("text/plain");
    return this;
  }

  public HttpResponse buildHttpResponse() {
    autoAddContentLengthHeader();
    log.debug("<<< ["+status+(contentStringForLog!=null ? "] "+contentStringForLog : "]"));
    DefaultFullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(httpVersion, status, byteBuf);
    fullHttpResponse.headers().add(headers);
    return fullHttpResponse;
  }

  protected void autoAddContentLengthHeader() {
    int readableBytes = byteBuf.readableBytes();
    header(CONTENT_LENGTH, Integer.toString(readableBytes));
  }

  public void send() {
    HttpResponse httpResponse = buildHttpResponse();
    if (!HttpHeaders.isKeepAlive(fullHttpRequest)) {
      channelHandlerContext
        .writeAndFlush(httpResponse)
        .addListener(ChannelFutureListener.CLOSE);
    } else {
      HttpHeaders headers = fullHttpRequest.headers();
      headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
      channelHandlerContext.writeAndFlush(httpResponse);
    }
    // when adding the ctx.close() it seems that this causes
    // the connection to be closed on the asyncHttpServer end, causing
    // exceptions on the client for subsequent requests.
    // ctx.close();
  }
}

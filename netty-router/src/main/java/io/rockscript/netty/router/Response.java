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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;

import static io.netty.buffer.Unpooled.buffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

public class Response {

  private static final Logger log = getLogger(Response.class);

  protected NettyServer nettyServer;
  protected ChannelHandlerContext channelHandlerContext;
  protected HttpVersion httpVersion = HTTP_1_1;
  protected ByteBuf byteBuf = buffer();
  protected HttpResponseStatus status = OK;
  protected HttpHeaders headers = new DefaultHttpHeaders();
  protected String contentStringForLog;

  public Response(NettyServer nettyServer, ChannelHandlerContext channelHandlerContext) {
    this.nettyServer = nettyServer;
    this.channelHandlerContext = channelHandlerContext;
  }

  public Response statusOk() {
    return status(OK);
  }

  public Response statusNotFound() {
    return status(NOT_FOUND);
  }

  public Response statusBadRequest() {
    return status(BAD_REQUEST);
  }

  public Response statusInternalServerError() {
    return status(INTERNAL_SERVER_ERROR);
  }

  public Response statusCreated() {
    return status(CREATED);
  }

  public Response statusNoContent() {
    return status(NO_CONTENT);
  }

  public Response status(HttpResponseStatus status) {
    this.status = status;
    return this;
  }

  public Response bodyJson(Object jsonBodyObject) {
    String jsonBodyString = nettyServer.getJsonHandler().toJsonString(jsonBodyObject);
    bodyString(jsonBodyString);
    headerContentTypeApplicationJson();
    return this;
  }

  public Response bodyString(String bodyString) {
    return bodyString(bodyString, UTF_8);
  }

  public Response bodyString(String content, Charset charset) {
    if (content!=null) {
      contentStringForLog = content; // the log is produced in getHttpResponse() below 
      byteBuf.writeBytes(content.getBytes(charset));
    }
    return this;
  }

  public Response header(String name, String value) {
    headers.add(name, value);
    return this;
  }

  public Response headerContentTypeApplicationJson() {
    headerContentType("application/json");
    return this;
  }

  public Response headerContentType(String contentType) {
    header(CONTENT_TYPE, contentType);
    return this;
  }

  public HttpResponse getHttpResponse() {
    autoAddContentLengthHeader();
    log.debug("<<< "+status+(contentStringForLog!=null ? " "+contentStringForLog : ""));
    DefaultFullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(httpVersion, status, byteBuf);
    fullHttpResponse.headers().add(headers);
    return fullHttpResponse;
  }

  protected void autoAddContentLengthHeader() {
    int readableBytes = byteBuf.readableBytes();
    header(CONTENT_LENGTH, Integer.toString(readableBytes));
  }
}

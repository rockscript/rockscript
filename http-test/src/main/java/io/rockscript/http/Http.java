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
package io.rockscript.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Fluent HTTP client based on Apache Http Components and for which
 * the HttpRequest and HttpResponse classes are serializable with Gson */
public class Http {

  protected static Logger log = LoggerFactory.getLogger(Http.class);

  protected CloseableHttpClient apacheHttpClient = HttpClients.createDefault();

  /** Used only when clients use methods {@link HttpRequest#setBodyObject(Object)} or
   * {@link HttpResponse#getBodyAs(java.lang.reflect.Type)}. */
  protected Codec codec;

  public Http() {
  }

  public Http(Codec codec) {
    this.codec = codec;
  }

  public void resetApacheHttpClient() {
    apacheHttpClient = HttpClients.createDefault();
  }

  public interface Methods {
    String GET = "GET";
    String PUT = "PUT";
    String POST = "POST";
    String DELETE = "DELETE";
  }

  public interface Headers {
    String CONTENT_TYPE = "Content-Type";
  }

  public interface ContentTypes {
    String APPLICATION_JSON = "application/json";
  }

  public interface ResponseCodes {
    int OK_200 = 200;
    int CREATED_201 = 201;
    int ACCEPTED_202 = 202;
    int NON_AUTHORATIVE_INFORMATION_203 = 203;
    int NO_CONTENT_204 = 204;
    int RESET_CONTENT_205 = 205;
    int PARTIAL_CONTENT = 206;

    int MULTIPLE_CHOICES_300 = 300;
    int MOVED_PERMANENTLY_301 = 301;
    int FOUND_302 = 302;
    int SEE_OTHER_303 = 303;
    int NOT_MODIFIED_304 = 304;
    int USE_PROXY_305 = 305;
    int TEMPORARY_REDIRECT_307 = 307;
    int PERMANENT_REDIRECT_308 = 308;

    int BAD_REQUEST_400 = 400;
    int UNQUTHORIZED_401 = 401;
    int PAYMENT_REQUIRED_402 = 402;
    int FORBIDDEN_403 = 403;
    int NOT_FOUND_404 = 404;
    int METHOD_NOT_ALLOWED_405 = 405;
    int NOT_ACCEPTABLE_406 = 406;
    int REQUEST_TIMEOUT_408 = 408;
    int CONFLICT_409 = 409;
    int GONE_410 = 410;

    int INTERNAL_SERVER_ERROR_500 = 500;
    int NOT_IMPLEMENTED_501 = 501;
    int BAD_GATEWAY_502 = 502;
    int SERVICE_UNAVAILABLE_503 = 503;
    int GATEWAY_TIMEOUT_504 = 504;
  }

  public HttpRequest newGet(String url) {
    return new HttpRequest(this, Methods.GET, url);
  }
  public HttpRequest newPut(String url) {
    return new HttpRequest(this, Methods.PUT, url);
  }
  public HttpRequest newPost(String url) {
    return new HttpRequest(this, Methods.POST, url);
  }
  public HttpRequest newDelete(String url) {
    return new HttpRequest(this, Methods.DELETE, url);
  }
  public HttpRequest newRequest(String method, String url) {
    return new HttpRequest(this, method, url);
  }

  public Codec getCodec() {
    if (codec==null) {
      throw new RuntimeException(
        "Http does not have a codec. HttpRequest.setBodyObject(Object) " +
        "and HttpResponse.getBodyAs(Type) need a codec.");
    }
    return codec;
  }

  public void setCodec(Codec codec) {
    this.codec = codec;
  }

  public CloseableHttpClient getApacheHttpClient() {
    return apacheHttpClient;
  }
}

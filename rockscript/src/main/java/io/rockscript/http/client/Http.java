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
package io.rockscript.http.client;

import com.google.gson.Gson;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/** Fluent, synchronous HTTP client based on Apache Http Components.
 *
 * To obtain a Http object, just use the constructor new Http();
 *
 * To start building a request, use the factory methods
 * {@link #newGet(String url)},
 * {@link #newPost(String url)},
 * {@link #newPut(String url)},
 * {@link #newDelete(String url)} or
 * {@link #newRequest(String method, String url)}
 *
 * To execute the request (synchronous) and get the response, use
 * {@link ClientRequest#execute()}
 *
 * This is a synchronous HTTP library.  That means that the client
 * thread will be blocked when executing the request until the response
 * is being obtained. If you need an async or non-blocking HTTP library,
 * go look elsewhere.
 *
 * The 2 motivations for writing this fluent API on top of Apache HTTP
 * commons are:
 * 1) ClientRequest and ClientResponse classes are serializable
 *    with Gson.
 * 2) Allow access to the response status line as well as the
 *    response body.
 */
public class Http {

  protected static Logger log = LoggerFactory.getLogger(Http.class);

  protected CloseableHttpClient apacheHttpClient = HttpClients.createDefault();

  protected Gson gson;

  public Http() {
  }

  public Http(Gson gson) {
    this.gson = gson;
  }

  public Gson getGson() {
    return gson;
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
    String APPLICATION_LD_JSON = "application/ld+json";
    String TEXT_PLAIN = "text/plain";
    String TEXT_HTML = "text/html";
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

    static String getText(int status) {
      return STATUS_TEXTS.get(status);
    }
  }

  private static final Map<Integer,String> STATUS_TEXTS = createStatusTexts();

  private static Map<Integer, String> createStatusTexts() {
    Map<Integer,String> statusTexts = new HashMap<>();
    for (Field field: ResponseCodes.class.getDeclaredFields()) {
      try {
        Integer statusCode = (Integer) field.get(null);
        String fieldName = field.getName();
        String statusText = fieldName.substring(0, fieldName.length()-4).replace('_',' ');
        statusTexts.put(statusCode, statusText);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Couldn't get status value with reflection from constant "+field);
      }
    }
    return statusTexts;
  }

  public ClientRequest newGet(String url) {
    return new ClientRequest(this, Methods.GET, url);
  }
  public ClientRequest newPut(String url) {
    return new ClientRequest(this, Methods.PUT, url);
  }
  public ClientRequest newPost(String url) {
    return new ClientRequest(this, Methods.POST, url);
  }
  public ClientRequest newDelete(String url) {
    return new ClientRequest(this, Methods.DELETE, url);
  }

  /** @param method constant can be obtained from {@link Methods} */
  public ClientRequest newRequest(String method, String url) {
    return new ClientRequest(this, method, url);
  }

  public CloseableHttpClient getApacheHttpClient() {
    return apacheHttpClient;
  }
}

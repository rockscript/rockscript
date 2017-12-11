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
package io.rockscript.http;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Http {
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

  public interface Methods {
    String GET = "GET";
    String PUT = "PUT";
    String POST = "POST";
    String DELETE = "DELETE";
  }

  public interface Headers {
    String CONTENT_TYPE = "Content-Type";
    String CONTENT_LENGTH = "Content-Length";
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
}

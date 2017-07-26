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
package io.rockscript.action.http;

import java.net.URL;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Request {

  private static Logger log = LoggerFactory.getLogger(Request.class);

  final URL url;
  final Method method;
  final RequestHeaders headers;
  final TextRequestBody body;

  Request(URL url, Method method, RequestHeaders headers) {
    this.url = url;
    this.method = method;
    this.headers = headers;
    body = null;
  }

  Request(URL url, Method method, RequestHeaders headers, TextRequestBody body) {
    this.url = url;
    this.method = method;
    this.headers = headers;
    this.body = body;
  }

  public boolean hasBody() {
    return body != null && !body.empty();
  }

  Optional<String> getHeader(String name) {
    return headers.get(name);
  }

  public void log() {
    StringBuilder message = new StringBuilder();
    message.append("HTTP ").append(method).append(" ").append(url).append("\n");
    message.append(headers);
    if (hasBody()) {
      message.append("\n");
      message.append(body);
    }
    log.debug(message.toString());
  }
}

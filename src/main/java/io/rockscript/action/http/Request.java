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
import java.util.Collection;
import java.util.Collections;

class Request {

  final URL url;
  final Method method;
  final Collection<RequestHeader> headers;
  final TextRequestBody body;

  Request(URL url, Method method) {
    this.url = url;
    this.method = method;
    headers = Collections.emptySet();
    body = null;
  }

  Request(URL url, Method method, Collection<RequestHeader> headers, TextRequestBody body) {
    this.url = url;
    this.method = method;
    this.headers = headers;
    this.body = body;
  }

  public boolean hasBody() {
    return body != null && !body.empty();
  }
}

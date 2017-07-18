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
 *
 */
package io.rockscript;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.asynchttpclient.Response;

public class HttpResponseJson {

  final int statusCode;
  final String statusText;
  final String contentType;
  final Map<String, List<String>> headers;
  final String body;

  HttpResponseJson(Response response) {
    statusCode = response.getStatusCode();
    statusText = response.getStatusText();
    contentType = response.getContentType();
    headers = response.getHeaders().entries().stream()
        .collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toList())));
    body = response.getResponseBody();
  }
}

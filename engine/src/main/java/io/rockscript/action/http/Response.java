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

import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;
import io.rockscript.engine.JsonReadable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Response implements JsonReadable {

  private static Logger log = LoggerFactory.getLogger(Response.class);

  final int status;
  final String statusText;
  // TODO Extract a TextResponseBody class, then a ResponseBody interface and add a BinaryResponseBody with byte[] content
  final String textBody;
  final ResponseHeaders headers;

  Response(int status, String statusText, String textBody, ResponseHeaders headers) {
    this.status = status;
    this.statusText = statusText;
    this.textBody = textBody;
    this.headers = headers;
  }

  Map<String, Object> json() throws IOException {
    if (!contentType().startsWith("application/json")) {
      throw new IllegalStateException("Cannot access non-JSON content as JSON");
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> map = HttpAction.gson.fromJson(textBody, Map.class);
    return map;
  }

  String contentType() throws IOException {
    return headers.values("Content-Type").stream().findFirst()
      .orElseThrow(() -> new IOException("Missing Content-Type response header"));
  }

  public void log() {
    StringBuilder message = new StringBuilder();
    message.append("HTTP ").append(status).append(" ").append(statusText).append("\n");
    message.append(headers);
    if (textBody!=null && !textBody.isEmpty()) {
      message.append("\n");
      message.append(textBody);
    }
    log.debug(message.toString());
  }

  @Override
  public Map<String, Object> get(String propertyName) {
    switch (propertyName) {
      case "body":
        try {
          return json();
        } catch (IOException e) {
          throw new IllegalArgumentException("Cannot read ‘body’ property: "+e.getMessage());
        }
      default:
        throw new IllegalArgumentException("Unsupported response property: "+propertyName);
    }
  }
}
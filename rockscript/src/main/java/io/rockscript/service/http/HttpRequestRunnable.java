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

package io.rockscript.service.http;

import com.google.gson.Gson;
import io.rockscript.Engine;
import io.rockscript.engine.impl.ContinuationReference;
import io.rockscript.engine.impl.ScriptRunner;
import io.rockscript.http.client.ClientRequest;
import io.rockscript.http.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestRunnable implements Runnable {

  static Logger log = LoggerFactory.getLogger(HttpRequestRunnable.class);

  ContinuationReference continuationReference;
  ClientRequest request;
  Engine engine;

  public HttpRequestRunnable(ContinuationReference continuationReference, ClientRequest request, Engine engine) {
    this.continuationReference = continuationReference;
    this.request = request;
    this.engine = engine;
  }

  @Override
  public void run() {
    Gson gson = engine.getGson();
    ClientResponse response = request
      .execute(Object.class);
    ScriptRunner scriptRunner = engine.getScriptRunner();
    scriptRunner.endFunction(continuationReference, response);
  }
}

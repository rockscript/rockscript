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

import io.rockscript.engine.job.RetryPolicy;
import io.rockscript.http.client.Http;
import io.rockscript.service.ImportObject;
import io.rockscript.service.ImportProvider;

public class HttpService extends ImportObject implements ImportProvider {

  private RetryPolicy defaultRetryPolicy = RetryPolicy.createDefaultRetryPolicy();

  public HttpService() {
    super("rockscript.io/http");
    put(new HttpServiceFunction(this, Http.Methods.GET));
    put(new HttpServiceFunction(this, Http.Methods.POST));
    put(new HttpServiceFunction(this, Http.Methods.PUT));
    put(new HttpServiceFunction(this, Http.Methods.DELETE));
  }

  @Override
  public ImportObject getImportObject() {
    return this;
  }

  public RetryPolicy getDefaultRetryPolicy() {
    return defaultRetryPolicy;
  }
}

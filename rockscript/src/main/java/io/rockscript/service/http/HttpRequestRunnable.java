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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.rockscript.Engine;
import io.rockscript.api.commands.ServiceFunctionErrorCommand;
import io.rockscript.engine.impl.ContinuationReference;
import io.rockscript.engine.impl.LockOperationEnd;
import io.rockscript.engine.impl.Time;
import io.rockscript.engine.job.RetryPolicy;
import io.rockscript.http.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.TemporalAmount;

public class HttpRequestRunnable implements Runnable {

  static Logger log = LoggerFactory.getLogger(HttpRequestRunnable.class);

  Engine engine;
  ContinuationReference continuationReference;
  HttpServiceClientRequest request;
  Integer failedAttemptsCount;
  RetryPolicy retryPolicy;

  public HttpRequestRunnable(Engine engine, ContinuationReference continuationReference, HttpServiceClientRequest request, Integer failedAttemptsCount, RetryPolicy retryPolicy) {
    this.engine = engine;
    this.continuationReference = continuationReference;
    this.request = request;
    this.failedAttemptsCount = failedAttemptsCount;
    this.retryPolicy = retryPolicy;
  }

  @Override
  public void run() {
    ClientResponse response = null;
    try {
      response = request.execute();

      Object responseObject = null;
      if (response.isContentTypeApplicationJson()) {
        responseObject = getResponseObjectWithParsedJsonBody(response);
      } else {
        responseObject = getResponseObjectWithOtherBody(response);
      }

      engine
        .getLockOperationExecutor()
        .executeInLock(new LockOperationEnd(continuationReference, responseObject));

    } catch (Exception e) {
      log.debug("Exception while executing HTTP "+request.getMethod()+" "+request.getUrl()+": "+e.getMessage(), e);

      Instant retry = null;
      if (retryPolicy!=null) {
        failedAttemptsCount = failedAttemptsCount!=null ? failedAttemptsCount+1 : 1;
        if (retryPolicy.size()>failedAttemptsCount) {
          TemporalAmount timeBeforeRetry = retryPolicy.get(failedAttemptsCount-1);
          retry = Time.now().plus(timeBeforeRetry);
        }
      }

      new ServiceFunctionErrorCommand()
        .continuationReference(continuationReference)
        .error(e.getMessage())
        .retry(retry)
        .execute(engine);
    }
  }

  private Object getResponseObjectWithParsedJsonBody(ClientResponse response) {
    Gson gson = engine.getGson();
    String bodyString = response.getBody();
    JsonParser parser = new JsonParser();
    JsonElement bodyObject = parser.parse(bodyString).getAsJsonObject();
    response.setBody(null);
    JsonObject responseJsonObject = gson.toJsonTree(response).getAsJsonObject();
    responseJsonObject.add("body", bodyObject);
    return gson.fromJson(responseJsonObject, Object.class);
  }

  private Object getResponseObjectWithOtherBody(ClientResponse response) {
    Gson gson = engine.getGson();
    JsonObject responseJsonObject = gson.toJsonTree(response).getAsJsonObject();
    return gson.fromJson(responseJsonObject, Object.class);
  }
}

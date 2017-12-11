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
package io.rockscript.engine.impl;

import com.google.gson.Gson;
import io.rockscript.service.AbstractServiceFunction;
import io.rockscript.service.ServiceFunctionInput;
import io.rockscript.service.ServiceFunctionOutput;
import io.rockscript.engine.EngineException;
import io.rockscript.http.client.ClientRequest;
import io.rockscript.http.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.rockscript.http.Http.ContentTypes.APPLICATION_JSON;
import static io.rockscript.http.Http.Headers.CONTENT_TYPE;

public class RemoteServiceFunction extends AbstractServiceFunction {

  static Logger log = LoggerFactory.getLogger(RemoteServiceFunction.class);

  String url;

  public RemoteServiceFunction(String url, String functionName) {
    super(url, functionName, null);
    if (!url.startsWith("http")) {
      this.url = "http://"+url;
    } else {
      this.url = url;
    }
    this.functionName = functionName;
    this.serviceName = url;
  }

  @Override
  public List<String> getArgNames() {
    return null;
  }

  @Override
  public ServiceFunctionOutput invoke(ServiceFunctionInput input) {
    Gson gson = input.getGson();
    String inputJson = gson.toJson(input);

    ContinuationReference continuationReference = input.getContinuationReference();
    String logPrefix = "["+continuationReference.getScriptExecutionId()+"|"+continuationReference.getExecutionId()+"]";

    ClientRequest request = input.getHttp()
      .newPost(url + "/" + functionName)
      .header(CONTENT_TYPE, APPLICATION_JSON)
      .body(inputJson);

    log.debug(request.toString(logPrefix));

    ClientResponse response = request.execute();

    log.debug(response.toString(logPrefix));

    int status = response.getStatus();
    if (status<200 || 300<status) {
      throw new EngineException("Remote HTTP serviceFunction did not return a status in the 200 range: "+status);
    }

    ServiceFunctionOutput output = null;
    try {
      output = response.getBodyAs(ServiceFunctionOutput.class);
    } catch (Exception e) {
      throw new EngineException("Couldn't parse remote HTTP serviceFunction response as ServiceFunctionOutput: " + e.getMessage(), e);
    }

    if (output!=null) {
      return output;
    } else {
      // The default async serviceFunction output is returned when the HTTP response is empty.
      return ServiceFunctionOutput.waitForFunctionEndCallback();
    }
  }

  @Override
  public String toString() {
    return url + "/" + functionName;
  }
}

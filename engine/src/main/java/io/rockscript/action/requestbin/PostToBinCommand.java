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
package io.rockscript.action.requestbin;

import java.util.Map;

import io.rockscript.ScriptService;
import io.rockscript.action.http.HttpResponse;

import static io.rockscript.action.http.HttpRequest.POST;

public class PostToBinCommand implements Runnable {

  ScriptService scriptService;
  String scriptExecutionId;
  String executionId;
  String message;

  public PostToBinCommand(String scriptExecutionId, String executionId, ScriptService scriptService, String message) {
    this.scriptExecutionId = scriptExecutionId;
    this.executionId = executionId;
    this.scriptService = scriptService;
    this.message = message;
  }

  @Override
  public void run() {
    HttpResponse createBinResponse = POST("https://requestb.in/api/v1/bins")
      .header("User-Agent", "curl/7.54.0")
      .execute();

    Map<String,Object> createBinResponseBody = (Map<String, Object>) createBinResponse.getBody();
    String binName = (String) createBinResponseBody.get("name");

    HttpResponse postResponse = POST("https://requestb.in/"+binName)
      .header("User-Agent", "curl/7.54.0")
      .body(message)
      .execute();

    String result = "Now try this in your browser: http://requestb.in/"+binName+"?inspect";

    scriptService.endWaitingAction(scriptExecutionId, executionId, result);
  }
}

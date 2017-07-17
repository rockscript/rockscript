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

package io.rockscript;

import java.util.concurrent.CompletableFuture;

import io.rockscript.engine.*;
import io.rockscript.test.TestEngine;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import org.asynchttpclient.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpActionTest {

  private EngineImpl engine;
  private DefaultAsyncHttpClient httpClient;
  private CompletableFuture<Response> future;

  @Before
  public void createHttpClient() throws Exception {
    httpClient = new DefaultAsyncHttpClient();
  }

  @Before
  public void createTestEngine() {
    engine = new TestEngine();
    ImportResolver importResolver = engine.getServiceLocator().getImportResolver();
    JsonObject httpService = new JsonObject()
        .put("get", this::sendHttpGetRequest);
    importResolver.add("rockscript.io/http", httpService);
  }

  private ActionResponse sendHttpGetRequest(FunctionInput input) {
    BoundRequestBuilder request = httpClient.prepareGet(input.getArg(0).toString())
        .setHeader(HttpHeaders.ACCEPT.toString(), MediaType.JSON_UTF_8.toString());

    future = request.execute()
        .toCompletableFuture()
        .thenApply(response -> {
          ArgumentsExpressionExecution argumentsExpressionExecution = input.getArgumentsExpressionExecution();
          String httpActionId = argumentsExpressionExecution.getId();
          String scriptExecutionId = argumentsExpressionExecution.getScriptExecution().getId();
          engine.endWaitingExecutionId(scriptExecutionId, httpActionId, new HttpResponseJson(response));
          return response;
        });
    return ActionResponse.waitForFunctionToCompleteAsync();
  }

  @Test
  public void testHttpAction() {
    // Given a script that requests a GitHub organisation
    String scriptId = engine.deployScript(
        "var http = system.import('rockscript.io/http'); \n" +
            "var organisation = http.get('https://api.github.com/orgs/RockScript');");

    // When I execute the script and wait for the response
    String scriptExecutionId = engine.startScriptExecution(scriptId);
    future.join();

    // Then the script result is stored in a variable.
    EventStore eventStore = engine.getServiceLocator().getEventStore();
    ScriptExecution scriptExecution = eventStore.loadScriptExecution(scriptExecutionId);
    assertNotNull(scriptExecution);

    Variable organisation = scriptExecution.getVariable("organisation");
    assertNotNull(organisation);
    HttpResponseJson response = (HttpResponseJson) organisation.getValue();
    assertNotNull(response);

    assertEquals(200, response.statusCode);
    assertEquals("OK", response.statusText);
    assertEquals("GitHub.com", response.headers.get("Server").get(0));
    assertEquals(MediaType.JSON_UTF_8.toString(), response.contentType);
    assertTrue(response.body.contains("\"name\":\"RockScript\""));
  }
}

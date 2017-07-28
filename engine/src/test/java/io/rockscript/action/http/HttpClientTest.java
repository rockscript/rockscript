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
package io.rockscript.action.http;

import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;

import org.asynchttpclient.*;
import org.asynchttpclient.Response;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Ignore
public class HttpClientTest {

  private static final String ORGANISATION_URL = "https://api.github.com/orgs/RockScript";
  private AsyncHttpClient httpClient;
  private int statusCode;

  @Before
  public void createHttpClient() throws Exception {
    httpClient = new DefaultAsyncHttpClient();
  }

  @Test
  public void testGetRequest() throws Exception {
    // Given an HTTP request for the GitHub API
    BoundRequestBuilder request = httpClient.prepareGet(ORGANISATION_URL)
        .setHeader(HttpHeaders.ACCEPT.toString(), MediaTypes.JSON_UTF_8);

    // When I execute the request
    ListenableFuture<org.asynchttpclient.Response> responseFuture = request.execute();

    // Then the future does not complete immediately.
    assertFalse(responseFuture.isDone());

    // When I wait for the future to complete
    org.asynchttpclient.Response response = responseFuture.get();

    // Then the response has the expected data.
    assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
    assertEquals(MediaTypes.JSON_UTF_8, response.getHeader(HttpHeaders.CONTENT_TYPE));
    assertTrue(response.getResponseBody().contains("\"name\":\"RockScript\""));
  }

  private void assertTrue(boolean contains) {
  }

  @Test
  public void testGetRequestWithEnd() throws Exception {
    // Given an HTTP request for the GitHub API
    BoundRequestBuilder request = httpClient.prepareGet(ORGANISATION_URL)
        .setHeader(HttpHeaders.ACCEPT.toString(), MediaTypes.JSON_UTF_8);

    // When I execute the request
    CompletableFuture<Response> future = request.execute().toCompletableFuture()
        .thenApply(response -> {
          statusCode = response.getStatusCode();
          return response;
        });

    // Then the future does not complete immediately.
    assertFalse(future.isDone());

    // When the future completes.
    future.join();

    // Then the status code has already been set.
    assertEquals(200, statusCode);
  }
}

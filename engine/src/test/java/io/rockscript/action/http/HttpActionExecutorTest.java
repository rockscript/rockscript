package io.rockscript.action.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import io.rockscript.action.ActionInput;
import io.rockscript.action.ActionResponse;
import io.rockscript.engine.*;
import io.rockscript.test.TestEngine;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpActionExecutorTest {

  private HttpActionExecutor workQueue;
  private EventStore eventStore;
  private TestEngine engine;

  @Before
  public void setup() throws Exception {
    engine = new TestEngine();
    workQueue = new HttpActionExecutor(engine);
    eventStore = engine.getServiceLocator().getEventStore();
    ImportResolver importResolver = engine.getServiceLocator().getImportResolver();
    JsonObject httpService = new JsonObject().put("get", this::queueHttpRequest);
    importResolver.add("rockscript.io/http", httpService);
  }

  // TODO Change to use an HttpAction object that was created by the import
  private ActionResponse queueHttpRequest(ActionInput input) {
    workQueue.addActionInput(input);
    return ActionResponse.waitForFunctionToCompleteAsync();
  }

  // TODO Test that the HTTP action can construct a Request object
//  @Test
  public void testHttpActionBuildsRequestFromInput() throws InterruptedException {
  }

  @Test
  public void testGetRequest() throws InterruptedException, IOException {
    // Given a script that uses an HTTP action
    String scriptId = engine.deployScript(
        "var http = system.import('rockscript.io/http'); \n" +
            "http.get();");

    // When I execute the script (to queue the input) and wait for execution to complete
    String scriptExecutionId = engine.startScriptExecution(scriptId);
    workQueue.executor.shutdown();
    workQueue.executor.awaitTermination(2, TimeUnit.SECONDS);

    // Then the action execution created an action ended event with the result
    List<EventJson> events = eventStore.findEventsByScriptExecutionId(scriptExecutionId);
    assertNotNull(events);
    assertFalse(events.isEmpty());
    Response httpResponse = events.stream()
        .filter(event -> event instanceof ActionEndedEventJson)
        .map(ActionEndedEventJson.class::cast)
        .map(actionEndedEvent -> actionEndedEvent.result)
        .map(ActionResponse.class::cast)
        .map(ActionResponse::getResult)
        .map(Response.class::cast)
        .findFirst().get();
    assertNotNull(httpResponse);

    // Add the response contains the expected data
    assertEquals(HttpURLConnection.HTTP_OK, httpResponse.status);
    assertEquals(MediaType.JSON_UTF_8.toString(), httpResponse.contentType());
    assertTrue(httpResponse.textBody.contains("\"name\":\"RockScript\""));
  }

  // TODO Test that the HTTP action can construct an HTTP POST Request object
//  @Test
  public void testPostRequestBody() throws InterruptedException {
  }
}

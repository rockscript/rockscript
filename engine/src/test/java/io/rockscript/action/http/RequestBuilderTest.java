package io.rockscript.action.http;

import java.util.List;

import io.rockscript.action.*;
import io.rockscript.engine.*;
import io.rockscript.test.TestEngine;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RequestBuilderTest {

  private EventStore eventStore;
  private TestEngine engine;

  private static class RequestBuilderAction implements Action {

    @Override
    public ActionOutput invoke(ActionInput input) {
      return ActionOutput.endFunction(new RequestBuilder(input).build());
    }
  }

  @Before
  public void setup() throws Exception {
    engine = new TestEngine();
    eventStore = engine.getServiceLocator().getEventStore();
    ImportResolver importResolver = engine.getServiceLocator().getImportResolver();
    JsonObject httpService = new JsonObject().put("request", new RequestBuilderAction());
    importResolver.add("rockscript.io/http", httpService);
  }

  @Test
  public void testHttpActionBuildsGetRequest() throws InterruptedException {
    // Given a script that uses an HTTP action
    String scriptId = engine.deployScript(
        "var http = system.import('rockscript.io/http'); \n" +
            "http.request({ " +
            "  url: 'https://api.github.com/orgs/RockScript'," +
            "  headers: { " +
            "    Accept: 'application/json' " +
            "  }" +
            "});");

    // When I execute the script
    String scriptExecutionId = engine.startScriptExecution(scriptId);

    // Then the action execution created an action ended event with the result
    List<EventJson> events = eventStore.findEventsByScriptExecutionId(scriptExecutionId);
    assertNotNull(events);
    assertFalse(events.isEmpty());
    Request httpRequest = events.stream()
        .filter(event -> event instanceof ActionEndedEventJson)
        .map(ActionEndedEventJson.class::cast)
        .map(actionEndedEvent -> actionEndedEvent.result)
        .map(Request.class::cast)
        .findFirst().get();
    assertNotNull(httpRequest);

    // Add the response contains the expected data
    assertEquals("https://api.github.com/orgs/RockScript", httpRequest.url.toString());
    assertEquals(Method.GET, httpRequest.method);
    assertEquals(scriptExecutionId, httpRequest.getHeader("X-Correlation-Id").get());
    assertEquals("application/json", httpRequest.getHeader("Accept").get());
  }

  @Test
  public void testPostRequestBody() throws InterruptedException {
    // Given a script that uses an HTTP action
    String scriptId = engine.deployScript(
        "var http = system.import('rockscript.io/http'); \n" +
            "http.request({ " +
            "  url: 'http://api.example.com/'," +
            "  method: 'post'," +
            "  headers: { " +
            "    'Content-Type': 'application/json' " +
            "  }," +
            "  body: { " +
            "    name: 'RockScript', " +
            "    url: 'http://rockscript.github.io/' " +
            "  }" +
            "});");

    // When I execute the script
    String scriptExecutionId = engine.startScriptExecution(scriptId);

    // Then the action execution created an action ended event with the result
    List<EventJson> events = eventStore.findEventsByScriptExecutionId(scriptExecutionId);
    assertNotNull(events);
    assertFalse(events.isEmpty());
    Request httpRequest = events.stream()
        .filter(event -> event instanceof ActionEndedEventJson)
        .map(ActionEndedEventJson.class::cast)
        .map(actionEndedEvent -> actionEndedEvent.result)
        .map(Request.class::cast)
        .findFirst().get();
    assertNotNull(httpRequest);

    // Add the response contains the expected data
    assertTrue(httpRequest.hasBody());
    assertEquals(Method.POST, httpRequest.method);
    assertEquals("application/json", httpRequest.body.contentType);
    // TODO Parse JSON body? i.e.
//    assertEquals("{\"name\":\"RockScript\",\"url\":\"http://rockscript.github.io/\"}", httpRequest.body.content);
  }
}

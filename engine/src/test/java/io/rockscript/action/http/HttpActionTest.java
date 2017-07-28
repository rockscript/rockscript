package io.rockscript.action.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import io.rockscript.TestEngine;
import io.rockscript.engine.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpActionTest {

  private EventStore eventStore;
  private TestEngine engine;

  @Before
  public void setup() throws Exception {
    engine = new TestEngine();
    eventStore = engine.getEngineConfiguration().getEventStore();
    ImportResolver importResolver = engine.getEngineConfiguration().getImportResolver();
    // TODO this should move into some EngineConfiguration thorugh ServiceLoader
    new HttpImportProvider().provideImport(importResolver);
  }

  @Test
  public void testGetRequest() throws InterruptedException, IOException {
    // Given a script that uses an HTTP action
    String scriptId = engine
      .deployScript(
        "var http = system.import('rockscript.io/http'); \n" +
        "http.request({ " +
        "  url: 'https://github.com/RockScript',"  +
        "  headers: { " +
        "    Accept: 'text/html' " +
        "  }" +
        "});")
      .getId();

    // When I execute the script
    String scriptExecutionId = engine
      .startScriptExecution(scriptId)
      .getId();

    // Then the action execution created an action ended event with the result
    List<ExecutionEventJson> events = eventStore.findEventsByScriptExecutionId(scriptExecutionId);
    assertNotNull(events);
    assertFalse(events.isEmpty());
    Response httpResponse = events.stream()
        .filter(event -> event instanceof ActionEndedEventJson)
        .map(ActionEndedEventJson.class::cast)
        .map(actionEndedEvent -> actionEndedEvent.result)
        .map(Response.class::cast)
        .findFirst().get();
    assertNotNull(httpResponse);

    // Add the response contains the expected data
    assertEquals(HttpURLConnection.HTTP_OK, httpResponse.status);
    assertEquals(MediaTypes.HTML_UTF_8, httpResponse.contentType());
    assertTrue(httpResponse.textBody.contains("<title>RockScript"));
  }

  @Test
  public void testJsonResponse() throws InterruptedException, IOException {
    // Given a script that uses an HTTP action
    String scriptId = engine
      .deployScript(
        "var http = system.import('rockscript.io/http'); \n" +
        "http.request({ " +
        "  url: 'https://api.github.com/orgs/RockScript',"  +
        "  headers: { " +
        "    Accept: 'application/json' " +
        "  }" +
        "});")
      .getId();

    // When I execute the script
    String scriptExecutionId = engine
      .startScriptExecution(scriptId)
      .getId();

    // Then the action execution created an action ended event with the result
    List<ExecutionEventJson> events = eventStore.findEventsByScriptExecutionId(scriptExecutionId);
    assertNotNull(events);
    assertFalse(events.isEmpty());
    Response httpResponse = events.stream()
        .filter(event -> event instanceof ActionEndedEventJson)
        .map(ActionEndedEventJson.class::cast)
        .map(actionEndedEvent -> actionEndedEvent.result)
        .map(Response.class::cast)
        .findFirst().get();
    assertNotNull(httpResponse);

    // Add the response contains the expected data
    assertEquals(HttpURLConnection.HTTP_OK, httpResponse.status);
    assertEquals(MediaTypes.JSON_UTF_8, httpResponse.contentType());
    assertTrue(httpResponse.json().containsKey("name"));
    assertEquals("RockScript", httpResponse.json().get("name"));
  }
}

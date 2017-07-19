package io.rockscript.action.http;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.rockscript.action.ActionInput;
import io.rockscript.engine.*;
import io.rockscript.test.TestEngine;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class HttpActionWorkQueueTest {

  private HttpActionWorkQueue workQueue;
  private EventStore eventStore;
  private TestEngine engine;

  @Before
  public void setup() throws Exception {
    engine = new TestEngine();
    workQueue = new HttpActionWorkQueue(engine);
    eventStore = engine.getServiceLocator().getEventStore();
    ImportResolver importResolver = engine.getServiceLocator().getImportResolver();
    JsonObject httpService = new JsonObject().put("get", this::queueHttpRequest);
    importResolver.add("rockscript.io/http", httpService);
  }

  private ActionResponse queueHttpRequest(FunctionInput input) {
    ArgumentsExpressionExecution execution = input.getArgumentsExpressionExecution();
    String scriptExecutionId = execution.getScriptExecution().getId();
    workQueue.addActionInput(new ActionInput(scriptExecutionId, execution.getId(), input.getArgs()));
    return ActionResponse.waitForFunctionToCompleteAsync();
  }

  @Test
  public void testHttpActionExecutes() throws InterruptedException {
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
    HttpResponse httpResponse = events.stream()
        .filter(event -> event instanceof ActionEndedEventJson)
        .map(ActionEndedEventJson.class::cast)
        .map(actionEndedEvent -> actionEndedEvent.result)
        .map(ActionResponse.class::cast)
        .map(ActionResponse::getResult)
        .map(HttpResponse.class::cast)
        .findFirst().get();
    assertEquals("42", httpResponse.body);
  }
}

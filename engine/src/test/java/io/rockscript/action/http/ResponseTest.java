package io.rockscript.action.http;

import java.io.IOException;

import io.rockscript.action.ActionOutput;
import io.rockscript.engine.ImportResolver;
import io.rockscript.engine.JsonObject;
import io.rockscript.test.TestEngine;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResponseTest {

  private TestEngine engine;
  private Object answer;

  @Before
  public void setup() throws Exception {
    engine = new TestEngine();
    ImportResolver importResolver = engine.getServiceLocator().getImportResolver();
    JsonObject answersService = new JsonObject()
      .put("ask", input -> {
          String body = "{ \"answer\": 42 }";
          ResponseHeaders headers = new ResponseHeaders(new ResponseHeader("Content-Type", "application/json"));
          Response response = new Response(200, "OK", body, headers);
          return ActionOutput.endFunction(response);
      })
      .put("log", input -> {
        answer = input.args.get(0);
        return ActionOutput.endFunction();
      });
    importResolver.add("example.com/answers", answersService);
  }

  @Test
  public void testJsonResponseBody() throws InterruptedException, IOException {
    // Given a script that references a JSON property in a JSON request body
    String scriptId = engine.deployScript(
      "var answers = system.import('example.com/answers'); \n" +
        "var response = answers.ask();" +
        "answers.log(response.body.answer);");

    // When I execute the script
    engine.startScriptExecution(scriptId);

    // Then the answer value was extracted from response.body
    assertEquals(42.0, answer);
  }
}

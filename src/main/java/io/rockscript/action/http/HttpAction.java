package io.rockscript.action.http;

import java.util.Collections;
import java.util.List;

import io.rockscript.action.Action;
import io.rockscript.action.ActionResponse;
import io.rockscript.engine.ArgumentsExpressionExecution;

public class HttpAction implements Action {

  Request request;

  @Override
  public ActionResponse invoke(ArgumentsExpressionExecution argumentsExpressionExecution, List<Object> args) {
    // TODO Construct the HTTP request from the inputs.
    String url = null;
    Method method = Method.GET;
    String contentType = null;
    TextRequestBody body = new TextRequestBody(contentType, "");
    request = new Request(url, method, Collections.emptySet(), body);

    // TODO Send the HTTP request using java.net.HttpURLConnection

    // TODO Construct a Response
    return ActionResponse.endFunction(new Response());
  }
}

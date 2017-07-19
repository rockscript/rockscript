package io.rockscript.action.http;

import java.util.List;

import io.rockscript.action.Action;
import io.rockscript.action.ActionResponse;
import io.rockscript.engine.ArgumentsExpressionExecution;

public class HttpAction implements Action {

  @Override
  public ActionResponse invoke(ArgumentsExpressionExecution argumentsExpressionExecution, List<Object> args) {
    // TODO Construct the HTTP request from the inputs.
    // TODO Send the HTTP request using java.net.HttpURLConnection
    // TODO Construct an HttpResponse
    return ActionResponse.endFunction(new HttpResponse());
  }
}

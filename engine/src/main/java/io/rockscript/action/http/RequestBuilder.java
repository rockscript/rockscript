package io.rockscript.action.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import io.rockscript.action.ActionInput;

public class RequestBuilder {

  private final ActionInput input;
  private Map<String, Object> arguments;

  RequestBuilder(ActionInput input) {
    this.input = input;
  }

  Request build() {
    final List<Object> rawArguments = input.args;
    if (rawArguments == null || rawArguments.size() != 1 || !(rawArguments.get(0) instanceof Map)) {
      throw new IllegalArgumentException("No arguments - pass an object with at least a ‘url’ property");
    }
    arguments = (Map<String, Object>) rawArguments.get(0);
    RequestHeaders headers = headers();
    if (hasBody()) {
      return new Request(url(), method(), headers, body(headers.get("Content-Type")));
    }
    else {
      return new Request(url(), method(), headers);
    }
  }

  private boolean hasBody() {
    return arguments.containsKey("body") && arguments.get("body") != null && !arguments.get("body").toString().isEmpty();
  }

  private TextRequestBody body(Optional<String> contentType) {
    // TODO Create the body type based on the Content-Type
    return new TextRequestBody("application/json", arguments.get("body").toString());
  }

  private URL url() {
    if (!arguments.containsKey("url")) {
      throw new IllegalArgumentException("Missing ‘url’ argument.");
    }
    Object url = arguments.get("url");
    try {
      return new URL(url.toString());
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(String.format("Invalid ‘url’ argument ‘%s’. Expected a valid URL.\n\n%s",
          url, e.getMessage()));
    }
  }

  private Method method() {
    if (!arguments.containsKey("method") || arguments.get("method") == null) {
      return Method.GET;
    }
    Object method = arguments.get("method");
    try {
      return Method.valueOf(method.toString().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(String.format("Invalid ‘method’ argument ‘%s’. Expected one of %s.\n\n%s",
          method, Method.names(), e.getMessage()));
    }
  }

  private RequestHeaders headers() {
    if (!arguments.containsKey("headers")) {
      return new RequestHeaders(input.context.scriptExecutionId);
    }
    if (arguments.get("headers") == null || !(arguments.get("headers") instanceof Map)) {
      throw new IllegalArgumentException("Invalid ‘headers’ argument. Expected an object with string properties.");
    }
    Map<String, String> headers = (Map<String, String>) arguments.get("headers");
    return new RequestHeaders(input.context.scriptExecutionId, headers);
  }
}

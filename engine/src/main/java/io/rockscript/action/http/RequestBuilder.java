package io.rockscript.action.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    return new Request(url(), method(), headers(), null);
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

  private RequestHeader correlationId() {
    return new RequestHeader("X-Correlation-Id", input.context.scriptExecutionId);
  }

  private Set<RequestHeader> headers() {
    if (!arguments.containsKey("headers")) {
      Set<RequestHeader> headers = new HashSet<>();
      headers.add(correlationId());
      return headers;
    }
    if (arguments.get("headers") == null || !(arguments.get("headers") instanceof Map)) {
      throw new IllegalArgumentException("Invalid ‘headers’ argument. Expected an object with string properties.");
    }
    Map<String, String> headers = (Map<String, String>) arguments.get("headers");
    return Stream.concat(Stream.of(correlationId()),
        headers.entrySet().stream().map(entry -> new RequestHeader(entry.getKey(), entry.getValue())))
        .collect(Collectors.toSet());
  }
}

package io.rockscript.action.http;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RequestHeaders {

  private final String scriptExecutionId;
  private final Set<RequestHeader> headers;

  RequestHeaders(String scriptExecutionId) {
    this.scriptExecutionId = scriptExecutionId;
    this.headers = Collections.emptySet();
  }

  RequestHeaders(String scriptExecutionId, Map<String, String> headers) {
    this.scriptExecutionId = scriptExecutionId;
    this.headers = headers.entrySet().stream()
        .map(entry -> new RequestHeader(entry.getKey(), entry.getValue()))
        .collect(Collectors.toSet());
  }

  Optional<String> get(String name) {
    return headers.stream().filter(header -> header.name.equals(name)).map(header -> header.value).findFirst();
  }

  Set<RequestHeader> values() {
    return Stream.concat(
        Stream.of(new RequestHeader("X-Correlation-Id", scriptExecutionId)),
        headers.stream()).collect(Collectors.toSet());
  }
}

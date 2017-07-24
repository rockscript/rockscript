package io.rockscript.action.http;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RequestHeaders {

  final Set<RequestHeader> headers;

  RequestHeaders(String scriptExecutionId) {
    headers = Stream.of(new RequestHeader("X-Correlation-Id", scriptExecutionId)).collect(Collectors.toSet());
  }

  RequestHeaders(String scriptExecutionId, Map<String, String> headers) {
    this.headers = Stream.concat(
        Stream.of(new RequestHeader("X-Correlation-Id", scriptExecutionId)),
        headers.entrySet().stream().map(entry -> new RequestHeader(entry.getKey(), entry.getValue())))
        .collect(Collectors.toSet());
  }

  Optional<String> get(String name) {
    return headers.stream().filter(header -> header.name.equals(name)).map(header -> header.value).findFirst();
  }
}

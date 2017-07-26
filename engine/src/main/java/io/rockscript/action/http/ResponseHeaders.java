package io.rockscript.action.http;

import java.util.*;
import java.util.stream.Collectors;

class ResponseHeaders {

  private final Collection<ResponseHeader> headers;

  ResponseHeaders(Map<String, List<String>> headers) {
    this.headers = headers.entrySet().stream()
        .map(entry -> new ResponseHeader(entry.getKey(), entry.getValue()))
        .collect(Collectors.toSet());
  }

  ResponseHeaders(ResponseHeader... headers) {
    this.headers = Arrays.stream(headers).collect(Collectors.toSet());
  }

  List<String> values(String headerName) {
    if (headerName == null || headers == null || headers.isEmpty()) {
      return Collections.emptyList();
    }
    Optional<ResponseHeader> responseHeader = headers.stream()
        .filter(header -> headerName.equals(header.name)).findFirst();
    return responseHeader.map(header -> header.values).orElse(Collections.emptyList());
  }

  @Override
  public String toString() {
    return headers.stream().map(Object::toString).collect(Collectors.joining("\n"));
  }
}

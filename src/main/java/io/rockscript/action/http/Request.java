package io.rockscript.action.http;

import java.util.Collection;
import java.util.Collections;

class Request {

  final String url;
  final Method method;
  final Collection<Header> headers;
  final TextRequestBody body;

  Request(String url, Method method) {
    this.url = url;
    this.method = method;
    headers = Collections.emptySet();
    body = null;
  }

  Request(String url, Method method, Collection<Header> headers, TextRequestBody body) {
    this.url = url;
    this.method = method;
    this.headers = headers;
    this.body = body;
  }
}

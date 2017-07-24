package io.rockscript.action.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

class HttpURLConnectionBuilder {

  private final HttpActionConfiguration configuration;
  private final Request request;

  HttpURLConnectionBuilder(HttpActionConfiguration configuration, Request request) {
    this.configuration = configuration;
    this.request = request;
  }

  HttpURLConnection build() throws IOException {
    HttpURLConnection connection = (HttpURLConnection) request.url.openConnection();
    connection.setRequestMethod(request.method.name());
    connection.setConnectTimeout(configuration.connectionTimeoutMilliseconds);
    connection.setReadTimeout(configuration.readTimeoutMilliseconds);
    if (request.hasBody()) {
      connection.addRequestProperty("Content-Type", request.body.contentType);
    }
    request.headers.values().forEach(header -> connection.addRequestProperty(header.name, header.value));

    if (request.method.hasRequestBody()) {
      connection.setDoOutput(true);
      try (OutputStream output = connection.getOutputStream()) {
        output.write(request.body.content.getBytes(Charset.forName("UTF-8")));
      }
    }

    return connection;
  }
}

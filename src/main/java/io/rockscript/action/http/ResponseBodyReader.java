package io.rockscript.action.http;

import java.io.*;
import java.net.HttpURLConnection;

class ResponseBodyReader {

  private static final int READ_BUFFER_SIZE_BYTES = 4096;
  private final HttpURLConnection connection;

  ResponseBodyReader(HttpURLConnection connection) {
    this.connection = connection;
  }

  byte[] read() throws IOException {
    boolean success = connection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST;
    try (InputStream response = success ? connection.getInputStream() : connection.getErrorStream()) {
      if (response == null) {
        return new byte[0];
      }

      // Read into byte array, from https://stackoverflow.com/a/17861016
      // In Java 9 replace with: return response.readAllBytes(); // From https://stackoverflow.com/a/37681322
      try (ByteArrayOutputStream responseBody = new ByteArrayOutputStream()) {
          byte[] buffer = new byte[READ_BUFFER_SIZE_BYTES];
          for (int bytesRead; (bytesRead = response.read(buffer)) != -1;) {
            responseBody.write(buffer, 0, bytesRead);
          }
          responseBody.flush();
          return responseBody.toByteArray();
      }
    }
  }
}

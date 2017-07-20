package io.rockscript.action.http;

class TextRequestBody {

  final String contentType;
  final String body;

  TextRequestBody(String contentType, String body) {
    this.contentType = contentType;
    this.body = body;
  }
}

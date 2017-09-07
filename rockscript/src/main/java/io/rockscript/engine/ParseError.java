package io.rockscript.engine;

public class ParseError {

  int line;
  int column;
  String message;

  // constructor for gson deserialization
  ParseError() {
  }

  public ParseError(int line, int column, String message) {
    this.line = line;
    this.column = column;
    this.message = message;
  }

  @Override
  public String toString() {
    return "[" + line +
           ":" + column +
           "] " + message;
  }

  public int getLine() {
    return line;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public int getColumn() {
    return column;
  }

  public void setColumn(int column) {
    this.column = column;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

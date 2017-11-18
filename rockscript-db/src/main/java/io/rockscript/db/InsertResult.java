package io.rockscript.db;

import java.sql.SQLException;

public class InsertResult {

  Insert insert;
  int rowCount = 0;
  SQLException exception = null;

  public InsertResult(Insert insert, int rowCount) {
    this.insert = insert;
    this.rowCount = rowCount;
  }

  public InsertResult(Insert insert, SQLException e) {
    this.insert = insert;
    this.exception = exception;
  }

  public InsertResult assertRowUpdated() {
    if (rowCount!=1) {
      throw new RuntimeException("Expected 1 row created, but rowCount was "+rowCount);
    }
    return this;
  }

  public InsertResult assertNoException() {
    if (exception!=null) {
      throw new DbException("Couldn't execute insert: "+ exception.getMessage(), exception);
    }
    return this;
  }
}

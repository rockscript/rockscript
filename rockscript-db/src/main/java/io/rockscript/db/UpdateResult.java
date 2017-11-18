package io.rockscript.db;

import java.sql.SQLException;

public class UpdateResult {

  Update update;
  int rowCount = 0;
  SQLException exception = null;

  public UpdateResult(Update update, int rowCount) {
    this.update = update;
    this.rowCount = rowCount;
  }

  public UpdateResult(Update update, SQLException e) {
    this.update = update;
    this.exception = exception;
  }

  public UpdateResult assertNoException() {
    if (exception!=null) {
      throw new DbException("Couldn't execute update: "+ exception.getMessage(), exception);
    }
    return this;
  }

  public UpdateResult assertRowsUpdated() {
    if (rowCount==0) {
      throw new RuntimeException("No row was updated during "+update.toString());
    }
    return this;
  }

  public int getRowCount() {
    return rowCount;
  }
}

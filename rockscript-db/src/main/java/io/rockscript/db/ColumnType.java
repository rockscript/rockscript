package io.rockscript.db;


import java.sql.ResultSet;

public abstract class ColumnType {

  Column column;

  public Column getColumn() {
    return column;
  }

  public void setColumn(Column column) {
    this.column = column;
  }

  public abstract String sql();

  protected SqlType getSqlType() {
    return column.getTable().getDb().getConfiguration().getSqlType();
  }

  public abstract <T> T getValue(ResultSet resultSet, int index);
}

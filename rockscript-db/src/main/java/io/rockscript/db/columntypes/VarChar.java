package io.rockscript.db.columntypes;

import io.rockscript.db.ColumnType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VarChar extends ColumnType {

  Integer n;

  public VarChar() {
  }

  public VarChar(Integer n) {
    this.n = n;
  }

  @Override
  public String sql() {
    return getSqlType().typeVarchar(n);
  }

  @Override
  public String getValue(ResultSet resultSet, int index) {
    try {
      return resultSet.getString(index);
    } catch (SQLException e) {
      throw new RuntimeException("Couldn't get string from result set: "+e.getMessage(), e);
    }
  }
}

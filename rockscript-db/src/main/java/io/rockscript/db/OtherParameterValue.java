package io.rockscript.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class OtherParameterValue implements ParameterValue {

  Object value;

  public OtherParameterValue(Object value) {
    this.value = value;
  }

  @Override
  public void set(PreparedStatement preparedStatement, int index) throws SQLException {
    preparedStatement.setObject(index, value, Types.OTHER);
  }

  public String getValueAsSql() {
    return value!=null ? value.toString() : null;
  }
}

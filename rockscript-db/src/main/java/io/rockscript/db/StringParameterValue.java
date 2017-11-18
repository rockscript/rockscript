package io.rockscript.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StringParameterValue implements ParameterValue {

  String value;

  public StringParameterValue(String value) {
    this.value = value;
  }

  @Override
  public String getValueAsSql() {
    return "'"+value+"'";
  }

  public void set(PreparedStatement preparedStatement, int index) throws SQLException {
    preparedStatement.setString(index, value);
  }
}

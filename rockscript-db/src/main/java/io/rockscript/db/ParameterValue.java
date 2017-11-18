package io.rockscript.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ParameterValue {

  String getValueAsSql();
  void set(PreparedStatement preparedStatement, int index) throws SQLException;
}

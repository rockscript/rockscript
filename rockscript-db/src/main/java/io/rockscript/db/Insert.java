package io.rockscript.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Insert extends ParametrizedDbOperation {

  static Logger log = LoggerFactory.getLogger(Insert.class);

  Table table;
  List<Column> columns = new ArrayList<>();

  public Insert(Tx tx, Table table) {
    super(tx);
    this.table = table;
  }

  public InsertResult execute() {
    try {
      String sql = toString();
      log.debug(tx+"\n" +sql);
      PreparedStatement preparedStatement = tx.getConnection().prepareStatement(sql);
      if (parameterValues!=null) {
        ParameterMap parameterMap = new ParameterMap();
        columns.forEach(column->parameterMap.add(column.getName()));
        setParameters(preparedStatement, parameterMap, log);
      }
      int rowCount = preparedStatement.executeUpdate();
      return new InsertResult(this, rowCount);
    } catch (SQLException e) {
      return new InsertResult(this, e);
    }
  }

  public Insert setString(Column column, String value) {
    return set(column, new StringParameterValue(value));
  }

  public Insert set(Column column, ParameterValue value) {
    columns.add(column);
    set(column.getName(), value);
    return this;
  }

}

package io.rockscript.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.stream.Collectors;

public class CreateTable {

  static Logger log = LoggerFactory.getLogger(CreateTable.class);

  Tx tx;
  Table table;

  public CreateTable(Tx tx, Table table) {
    this.tx = tx;
    this.table = table;
  }

  public void execute() {
    try {
      String sql = toString();
      log.debug(tx+" " +sql);
      int result = tx.getConnection()
        .createStatement()
        .executeUpdate(sql);
    } catch (SQLException e) {
      throw new RuntimeException("Couldn't create table: "+e.getMessage(), e);
    }
  }

  public String toString() {
    return "CREATE TABLE " + table.getName() + " (\n" +
           table.getColumns().values().stream()
             .map(column->column.createSql())
             .collect(Collectors.joining(",\n")) +
           ")";
  }

  public Table getTable() {
    return table;
  }
}

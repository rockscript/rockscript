package io.rockscript.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class DropTable {

  static Logger log = LoggerFactory.getLogger(DropTable.class);

  Tx tx;
  Table table;

  public DropTable(Tx tx, Table table) {
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
      throw new RuntimeException("Couldn't drop table: "+e.getMessage(), e);
    }
  }

  public String toString() {
    return  "DROP TABLE "+table.getName()+" CASCADE";
  }

  public Table getTable() {
    return table;
  }
}

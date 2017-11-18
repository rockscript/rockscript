package io.rockscript.db;

import io.rockscript.db.columntypes.VarChar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.rockscript.db.WhereCondition.and;
import static io.rockscript.db.WhereCondition.equal;
import static io.rockscript.db.WhereCondition.isNull;

public class SchemaUpdatesTable extends Table {

  public static final Column ID = new Column("id", new VarChar()).primaryKey();
  public static final Column TABLE_NAME = new Column("table_name", new VarChar());
  public static final Column UPDATE = new Column("update", new VarChar());

  private static final String ID_DATABASE_LOCK = "database-lock";
  private static final String ALL_TABLES = "*";

  public SchemaUpdatesTable() {
    super("schema_updates",
      ID,
      TABLE_NAME,
      UPDATE);
  }

  public Map<String,List<String>> findAllSchemaUpdates(Tx tx) {
    Map<String,List<String>> schemaUpdates = new HashMap<>();
    SelectResult selectResult = tx.newSelect()
      .from(this)
      .execute();
    while (selectResult.next()) {
      String tableName = selectResult.get(TABLE_NAME);
      String update = selectResult.get(UPDATE);
      List<String> tableUpdates = schemaUpdates.get(tableName);
      if (tableUpdates==null) {
        tableUpdates = new ArrayList<>();
        schemaUpdates.put(tableName, tableUpdates);
      }
      tableUpdates.add(update);
    }
    return schemaUpdates;
  }

  public void insertLockRow(Tx tx) {
    tx.newInsert(this)
      .setString(ID, ID_DATABASE_LOCK)
      .setString(TABLE_NAME, ALL_TABLES)
      .execute()
      .assertRowUpdated()
      .assertNoException();
  }

  public boolean acquireSchemaLock(Tx tx, SchemaUpdatesTable schemaUpdatesTable) {
    int rowCount = tx.newUpdate(this)
      // .setString(UPDATE, "locked")
      .where(and(equal(ID, ID_DATABASE_LOCK),
                 equal(TABLE_NAME, ALL_TABLES),
                 isNull(UPDATE)))
      .execute()
      .assertNoException()
      .getRowCount();
    return rowCount==1;
  }

  public void releaseSchemaLock() {
    throw new UnsupportedOperationException();
  }
}

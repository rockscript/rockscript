package io.rockscript.db;

public class Column {

  Table table;
  String name;
  ColumnType type;
  boolean primaryKey;

  public Column(String name, ColumnType type) {
    this.name = name;
    this.type = type;
    type.setColumn(this);
  }

  public String getName() {
    return name;
  }

  public ColumnType getType() {
    return type;
  }

  public Column primaryKey() {
    primaryKey = true;
    return this;
  }

  public boolean isPrimaryKey() {
    return primaryKey;
  }

  public Table getTable() {
    return table;
  }

  public void setTable(Table table) {
    this.table = table;
  }

  public String createSql() {
    return "  "+name+" "+type.sql()+
           (primaryKey ? " CONSTRAINT "+table.getName()+"_pkey PRIMARY KEY" : "");
  }
}

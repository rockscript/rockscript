package io.rockscript.db;

public abstract class SelectField {

  public abstract String sql(Select select);

  public static class ColumnField extends SelectField {
    Column column;
    public ColumnField(Column column) {
      this.column = column;
    }

    @Override
    public String sql(Select select) {
      String tableAlias = select.getFrom().get(column.getTable());
      return (tableAlias!=null ? tableAlias+"."+column.getName() : column.getName());
    }
  }
}

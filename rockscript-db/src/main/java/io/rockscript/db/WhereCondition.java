package io.rockscript.db;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class WhereCondition {

  public abstract void mapParameterNames(ParameterMap parameterMap);

  public abstract String sql();

  public static WhereCondition equal(final Column column, final String parameterName) {
    return new WhereCondition() {
      @Override
      public void mapParameterNames(ParameterMap parameterMap) {
        parameterMap.add(parameterName);
      }
      @Override
      public String sql() {
        return column.getName()+" = ?";
      }
    };
  }

  public static WhereCondition and(final WhereCondition... andConditions) {
    return new WhereCondition() {
      @Override
      public void mapParameterNames(ParameterMap parameterMap) {
        Arrays.stream(andConditions)
          .forEach(andCondition->andCondition.mapParameterNames(parameterMap));
      }
      @Override
      public String sql() {
        return "(\n      " +
          Arrays.stream(andConditions)
            .map(andCondition->andCondition.sql())
            .collect(Collectors.joining("\n      AND "))+
          ")";
      }
    };
  }

  public static WhereCondition isNull(final Column column) {
    return new WhereCondition() {
      @Override
      public void mapParameterNames(ParameterMap parameterMap) {
      }
      @Override
      public String sql() {
        return column.getName()+" IS NULL";
      }
    };
  }
}

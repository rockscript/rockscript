/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.rockscript.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Select extends ParametrizedDbOperation {
  
  private static final Logger log = LoggerFactory.getLogger(Select.class);

  /** columns==null means 'SELECT *' */
  List<SelectField> fields;
  Map<Column,Integer> columnFieldIndexes;
  Map<Table,String> from;
  WhereCondition where;

  public Select(Tx tx) {
    super(tx);
  }

  public SelectResult execute() {
    if (fields==null) {
      for (Table table: from.keySet()) {
        table.getColumns().values()
          .forEach(column->this.field(column));
      }
    }
    try {
      tx.logSQL(toString(true));
      String sql = toString();
      PreparedStatement preparedStatement = tx.getConnection().prepareStatement(sql);
      if (parameters!=null) {
        setParameters(preparedStatement);
      }
      ResultSet resultSet = preparedStatement.executeQuery();
      return new SelectResult(this, resultSet);
    } catch (SQLException e) {
      return new SelectResult(this, e);
    }
  }

  public Select field(Column column) {
    field(new SelectField.ColumnField(column));
    if (columnFieldIndexes==null) {
      columnFieldIndexes = new HashMap<>();
    }
    columnFieldIndexes.put(column, fields.size());
    return this;
  }

  public Select field(SelectField field) {
    if (fields==null) {
      fields = new ArrayList<>();
    }
    fields.add(field);
    return this;
  }

  public Select from(Table table) {
    return from(table, null);
  }

  public Select from(Table table, String alias) {
    if (from==null) {
      from = new LinkedHashMap<>();
    }
    from.put(table, alias);
    return this;
  }

  public String toString(boolean showParameterValues) {
    return "SELECT " + fieldsSql() +
           (from!=null ? "\nFROM "+fromSql() : "") +
           (where!=null ? "\nWHERE "+where.toString(showParameterValues) : "");
  }

  String fieldsSql() {
    if (fields==null) {
      return "*";
    }
    return fields.stream()
      .map(field->field.sql(this))
      .collect(Collectors.joining(",\n       "));
  }

  String fromSql() {
    return from.keySet().stream()
      .map(this::fromTableSql)
      .collect(Collectors.joining(",\n     "));
  }

  String fromTableSql(Table table) {
    String alias = from.get(table);
    return alias!=null ? table.getName()+" AS "+alias : table.getName();
  }

  public List<SelectField> getFields() {
    return fields;
  }

  public Map<Table, String> getFrom() {
    return from;
  }

  public WhereCondition getWhere() {
    return where;
  }

  public void setWhere(WhereCondition where) {
    this.where = where;
    where.setParameters(this);
  }

  public Select where(WhereCondition where) {
    setWhere(where);
    return this;
  }
}

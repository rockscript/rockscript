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
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Insert extends ParametrizedDbOperation {

  static Logger log = LoggerFactory.getLogger(Insert.class);

  Table table;
  Map<Column,Parameter> columns = new LinkedHashMap<>();

  public Insert(Tx tx, Table table) {
    super(tx);
    this.table = table;
  }

  public InsertResult execute() {
    try {
      tx.logSQL(toString(true));
      String sql = toString();
      PreparedStatement preparedStatement = tx.getConnection().prepareStatement(sql);
      if (parameters!=null) {
        setParameters(preparedStatement);
      }
      int rowCount = preparedStatement.executeUpdate();
      return new InsertResult(this, rowCount);
    } catch (SQLException e) {
      return new InsertResult(this, e);
    }
  }

  @Override
  public String toString(boolean showParameterNames) {
    return
      "INSERT INTO "+table.getName()+ " (" +
      columns.keySet().stream()
        .map(column->column.getName())
        .collect(Collectors.joining(",")) + ") \n" +
      "VALUES ( " +
      columns.values().stream()
        .map(parameter->(showParameterNames ? parameter.toString() : "?"))
        .collect(Collectors.joining(", ")) + " )";
  }

  public Insert valueString(Column column, String value) {
    return set(column, setParameterString(value));
  }

  public Insert set(Column column, Parameter parameter) {
    columns.put(column, parameter);
    return this;
  }

}

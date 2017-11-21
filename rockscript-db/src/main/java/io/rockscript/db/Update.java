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

public class Update extends ParametrizedDbOperation {
  
  private static final Logger log = LoggerFactory.getLogger(Update.class);

  Table table;
  Map<Column,Parameter> sets = new LinkedHashMap<>();
  WhereCondition where;

  public Update(Tx tx, Table table) {
    super(tx);
    this.table = table;
  }

  public UpdateResult execute() {
    try {
      tx.logSQL(toString(true));
      String sql = toString();
      PreparedStatement preparedStatement = tx.getConnection().prepareStatement(sql);
      if (parameters!=null) {
        setParameters(preparedStatement);
      }
      int rowCount = preparedStatement.executeUpdate();
      return new UpdateResult(this, rowCount);
    } catch (SQLException e) {
      return new UpdateResult(this, e);
    }
  }

  public String toString() {
    return toString(false);
  }

  public String toString(boolean showParameterNames) {
    return "UPDATE "+table.getName()+" \n" +
           "SET " +
           sets.entrySet().stream()
             .map(entry->(entry.getKey().getName()+" = "+(showParameterNames ? entry.getValue() : "?")))
             .collect(Collectors.joining(",\n      ")) +
           (where!=null ? "\nWHERE "+where.toString(showParameterNames) : "");
  }

  public Update setString(Column column, String value) {
    Parameter parameter = setParameterString(value);
    sets.put(column, parameter);
    return this;
  }

  public Update setOther(Column column, Object value) {
    Parameter parameter = setParameterOther(value);
    sets.put(column, parameter);
    return this;
  }

  public WhereCondition getWhere() {
    return this.where;
  }
  public void setWhere(WhereCondition where) {
    this.where = where;
    where.setParameters(this);
  }
  public Update where(WhereCondition where) {
    setWhere(where);
    return this;
  }
}

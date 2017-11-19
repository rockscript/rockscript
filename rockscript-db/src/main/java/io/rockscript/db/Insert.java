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

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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class SelectResult {
  
  private static final Logger log = LoggerFactory.getLogger(SelectResult.class);

  Select select;
  ResultSet resultSet;
  Map<String,Object> result;
  SQLException exception;

  public SelectResult(Select select, ResultSet resultSet) {
    this.select = select;
    this.resultSet = resultSet;
  }

  public SelectResult(Select select, SQLException exception) {
    this.select = select;
    this.exception = exception;
  }

  public void close() {
    try {
      resultSet.close();
    } catch (SQLException e) {
      throw new RuntimeException("Couldn't close: "+e.getMessage(), e);
    }
  }

  public boolean next() {
    try {
      return resultSet.next();
    } catch (SQLException e) {
      throw new RuntimeException("Couldn't get next result: "+e.getMessage(), e);
    }
  }

  public ResultSet getResultSet() {
    return resultSet;
  }

  public <T> T get(Column column) {
    Integer index = select.columnFieldIndexes.get(column);
    return column.getType().getValue(resultSet, index);
  }

  public SelectResult assertNoException() {
    if (exception!=null) {
      throw new DbException("Couldn't execute select: "+ exception.getMessage(), exception);
    }
    return this;
  }

}

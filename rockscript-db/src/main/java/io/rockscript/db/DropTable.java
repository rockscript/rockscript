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
      tx.logSQL(sql);
      int result = tx.getConnection()
        .createStatement()
        .executeUpdate(sql);
    } catch (SQLException e) {
      throw new RuntimeException("Couldn't drop table: "+e.getMessage(), e);
    }
  }

  public String toString() {
    return  "DROP TABLE "+table.getName();
  }

  public Table getTable() {
    return table;
  }
}

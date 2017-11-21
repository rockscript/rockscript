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

import java.sql.SQLException;

public class InsertResult {

  Insert insert;
  int rowCount = 0;
  SQLException exception = null;

  public InsertResult(Insert insert, int rowCount) {
    this.insert = insert;
    this.rowCount = rowCount;
  }

  public InsertResult(Insert insert, SQLException e) {
    this.insert = insert;
    this.exception = exception;
  }

  public InsertResult assertInsertHappened() {
    if (rowCount!=1) {
      throw new RuntimeException("Expected 1 row created, but rowCount was "+rowCount);
    }
    if (exception!=null) {
      throw new DbException("Couldn't execute insert: "+ exception.getMessage(), exception);
    }
    return this;
  }
}

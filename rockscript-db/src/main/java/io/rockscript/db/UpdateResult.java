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

public class UpdateResult {

  Update update;
  int rowCount = 0;
  SQLException exception = null;

  public UpdateResult(Update update, int rowCount) {
    this.update = update;
    this.rowCount = rowCount;
  }

  public UpdateResult(Update update, SQLException e) {
    this.update = update;
    this.exception = exception;
  }

  public UpdateResult assertNoException() {
    if (exception!=null) {
      throw new DbException("Couldn't execute update: "+ exception.getMessage(), exception);
    }
    return this;
  }

  public UpdateResult assertRowsUpdated() {
    if (rowCount==0) {
      throw new RuntimeException("No row was updated during "+update.toString());
    }
    return this;
  }

  public int getRowCount() {
    return rowCount;
  }
}

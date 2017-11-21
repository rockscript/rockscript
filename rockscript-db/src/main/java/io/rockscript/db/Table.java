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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Table {
  
  protected String name;
  protected Map<String,Column> columns;
  protected Db db;

  public Table(String name, Column... columns) {
    this.name = name;
    this.columns = new LinkedHashMap<>();
    Arrays.stream(columns)
      .forEach(column->{
        this.columns.put(column.getName(), column);
        column.setTable(this);
      });
  }

  public boolean isUpToDate(List<String> tableUpdates) {
    return true;
  }

  public <T> T tx(TxLogic txLogic) {
    return db.tx(txLogic);
  }

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Table name(String name) {
    this.name = name;
    return this;
  }

  public void setDb(Db db) {
    this.db = db;
  }
  public Db getDb() {
    return db;
  }

  public Map<String, Column> getColumns() {
    return columns;
  }
}

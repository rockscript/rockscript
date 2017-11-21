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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class Tx {

  private static final Logger log = LoggerFactory.getLogger(Tx.class);
  
  private static long nextTxId = 1;
  
  protected long id = nextTxId++;
  protected Db db;
  protected Connection connection;
  protected boolean isRollbackOnly = false;
  protected Object result;
  protected Throwable rollbackReason;

  public Tx(Db db, Connection connection) {
    this.db = db;
    this.connection = connection;
    log.debug("Starting "+this);
  }
  
  public String toString() {
    return "Tx"+id+"";
  }

  public Db getDb() {
    return this.db;
  }

  public Connection getConnection() {
    return this.connection;
  }

  public Object getResult() {
    return this.result;
  }
  public void setResult(Object result) {
    this.result = result;
  }
  public Tx result(Object result) {
    this.result = result;
    return this;
  }

  public void setRollbackOnly() {
    setRollbackOnly(null);
  }
  
  public void setRollbackOnly(Throwable rollbackReason) {
    this.isRollbackOnly = true;
    this.rollbackReason = rollbackReason;
  }

  public boolean isRollbackOnly() {
    return isRollbackOnly;
  }

  public Update newUpdate(Table table) {
    return new Update(this, table);
  }

  public Select newSelect() {
    return new Select(this);
  }

  public CreateTable newCreateTable(Table table) {
    return new CreateTable(this, table);
  }

  public DropTable newDropTable(Table table) {
    return new DropTable(this, table);
  }

  public SqlType getSqlType() {
    return db.getSqlType();
  }

  protected void end() {
    if (isRollbackOnly) {
      try {
        log.error("Rolling back "+this, rollbackReason);
        connection.rollback();
      } catch (SQLException e) {
        log.error("Tx rollback failed: "+e.getMessage(), e);
      }
    } else {
      try {
        log.error("Committing "+this);
        connection.commit();
      } catch (SQLException e) {
        log.error("Tx commit failed: "+e.getMessage(), e);
      }
    }
  }

  public Set<String> getTableNames() {
    try {
      Set<String> tableNames = new HashSet<>();
      DatabaseMetaData metaData = connection.getMetaData();
      ResultSet resultSet = metaData.getTables(null, null, "%", null);
      while (resultSet.next()) {
        tableNames.add(resultSet.getString(3));
      }
      return tableNames;
    } catch (SQLException e) {
      throw new DbException("Couldn't get metadata: "+e.getMessage(), e);
    }
  }

  public Insert newInsert(Table table) {
    return new Insert(this, table);
  }

  public String generateId() {
    return db.getConfiguration().getIdGenerator().generateId();
  }

  public void logSQL(String sql) {
    db.logSQL(this, sql);
  }
}

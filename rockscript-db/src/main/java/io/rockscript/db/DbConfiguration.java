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

import io.rockscript.db.id.UuIdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/** Configuration properties for the database connection.
 *
 * Default configurations are
 *   server = "localhost";
 *   databaseName = "test";
 *   username = "test";
 *   password = "test";
 * 
 * @author Tom Baeyens
 */
public class DbConfiguration {

  protected String host = "localhost";
  protected Integer port;
  protected String databaseName = "test";
  protected String connectionUrl;
  protected String username = "test";
  protected String password = "test";
  protected IdGenerator idGenerator = new UuIdGenerator();
  protected SqlType sqlType;

  protected List<Table> tables = new ArrayList<>();
  protected SchemaUpdatesTable schemaUpdatesTable = new SchemaUpdatesTable();
  protected List<SchemaUpdate> schemaUpdates = new ArrayList<>();

  public Db build() {
    return new Db(this);
  }

  public DbConfiguration table(Table table) {
    this.tables.add(table);
    return this;
  }
  public List<Table> getTables() {
    return tables;
  }
  public void setTables(List<Table> tables) {
    this.tables = tables;
  }

  public String getHost() {
    return this.host;
  }
  /** The IP address or server domain name.
   * Simplest way to specify the server.
   * Always specify the databaseName as well 
   * when using this configuration. 
   * Alternatively specify the full JDBC connection 
   * url with {@link #connectionUrl}. */
  public void setHost(String host) {
    this.host = host;
  }
  /** Simplest way to specify the server: put in an IP address or 
   * the server domain name. */
  public DbConfiguration host(String host) {
    this.host = host;
    return this;
  }
  
  public Integer getPort() {
    return this.port;
  }
  /** Optional configuration when specifying the server property. */
  public void setPort(Integer port) {
    this.port = port;
  }
  /** Optional configuration when specifying the server property. */
  public DbConfiguration port(Integer port) {
    this.port = port;
    return this;
  }
  
  public String getDatabaseName() {
    return this.databaseName;
  }
  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }
  public DbConfiguration databaseName(String databaseName) {
    this.databaseName = databaseName;
    return this;
  }
  
  public String getConnectionUrl() {
    return this.connectionUrl;
  }
  /** The jdbc connection url, which overrules properties 
   * server, port and databaseName. */
  public void setConnectionUrl(String connectionUrl) {
    this.connectionUrl = connectionUrl;
  }
  /** The jdbc connection url, which overrules properties 
   * server, port and databaseName. */
  public DbConfiguration connectionUrl(String connectionUrl) {
    this.connectionUrl = connectionUrl;
    return this;
  }
  
  public String getUsername() {
    return this.username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  public DbConfiguration username(String username) {
    this.username = username;
    return this;
  }
  
  public String getPassword() {
    return this.password;
  }
  public void setPassword(String password) {
    this.password = password;
  }
  public DbConfiguration password(String password) {
    this.password = password;
    return this;
  }

  public IdGenerator getIdGenerator() {
    return this.idGenerator;
  }
  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }
  public DbConfiguration idGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
    return this;
  }

  public SqlType getSqlType() {
    return this.sqlType;
  }
  public void setSqlType(SqlType sqlType) {
    this.sqlType = sqlType;
  }
  public DbConfiguration dbType(SqlType sqlType) {
    this.sqlType = sqlType;
    return this;
  }

  public SchemaUpdatesTable getSchemaUpdatesTable() {
    return this.schemaUpdatesTable;
  }
  public void setSchemaUpdatesTable(SchemaUpdatesTable schemaUpdatesTable) {
    this.schemaUpdatesTable = schemaUpdatesTable;
  }
  public DbConfiguration schemaHistoryTable(SchemaUpdatesTable schemaHistoryTable) {
    this.schemaUpdatesTable = schemaHistoryTable;
    return this;
  }

  public List<SchemaUpdate> getSchemaUpdates() {
    return schemaUpdates;
  }

  /**
   * Eg new SchemaUpdates()
   *     .add("Added column x", (tx,createdTables)-> {
   *       tx.newCreateTable(...)
   *     })
   *     .add("Changed date type", (tx,createdTables)-> {
   *     });
   *
   * CreatedTables is the list of tables that was created.  Tables are
   * created in the latest version of the schema.  So this may be necessary
   * for the update to know if parts can be skipped. */
  public DbConfiguration schemaUpdate(String name, BiConsumer<Tx,CreatedTablesList> update) {
    if (schemaUpdates.stream()
      .filter(s->name.equals(s.getName()))
      .findFirst()
      .isPresent()) {
      throw new DbException("Duplicate update name: "+name);
    }
    schemaUpdates.add(new SchemaUpdate(name, update));
    return this;
  }
}

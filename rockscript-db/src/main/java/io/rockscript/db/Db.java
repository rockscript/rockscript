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

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Tom Baeyens
 */
public class Db {
  
  private static final Logger log = LoggerFactory.getLogger(Db.class);

  protected DataSource dataSource;
  protected DbConfiguration configuration;

  public Db(DbConfiguration configuration) {
    this.configuration = configuration;
    DbException.throwIfNull(configuration.getSqlType(), "sqlType is null");
    configuration.getTables().forEach(table->table.setDb(this));
    configuration.getSchemaUpdatesTable().setDb(this);

    try {
      ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
      this.dataSource = comboPooledDataSource;
      comboPooledDataSource.setDriverClass(getSqlType().getDriverClassName()); //loads the jdbc driver
      
      String connectionUrl = configuration.getConnectionUrl();
      if (connectionUrl==null) {
        String server = configuration.getHost();
        Integer port = configuration.getPort();
        String databaseName = configuration.getDatabaseName();
        connectionUrl = getSqlType().getConnectionUrl(server, port, databaseName);
      }
      comboPooledDataSource.setJdbcUrl(connectionUrl);
      
      comboPooledDataSource.setUser(configuration.getUsername());
      comboPooledDataSource.setPassword(configuration.getPassword());

      comboPooledDataSource.setAcquireRetryAttempts(1);
      comboPooledDataSource.setMinPoolSize(1);

//    // the settings below are optional -- c3p0 can work with defaults
//    ds.setMinPoolSize(5);                                     
//    ds.setAcquireIncrement(5);
//    ds.setMaxPoolSize(20);
      
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }                                  
  }

  public SqlType getSqlType() {
    return configuration.getSqlType();
  }

  @SuppressWarnings("unchecked")
  public <T> T tx(TxLogic txLogic) {
    Connection connection = null;
    Tx tx = null;
    Exception exception = null;
    try {
      connection = dataSource.getConnection();
      connection.setAutoCommit(false);
      tx = new Tx(this, connection);
      txLogic.execute(tx);
    } catch (Exception e) {
      exception = e;
      tx.setRollbackOnly(e);
    } 
    if (tx!=null) {
      tx.end();
    }
    if (connection!=null) {
      try {
        connection.close();
      } catch (SQLException e) {
        log.error("Tx connection close: "+e.getMessage(), e);
      }
    }
    if (exception!=null) {
      if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else {
        throw new RuntimeException("Transaction failed: "+exception.getMessage(), exception);
      }
    }
    return tx!=null ? (T) tx.getResult() : null;
  }


  /** Blocks till the schema is up to date. This may take a
   * while as it potentially is waiting for another node
   * to complete the update. */
  public void updateSchema() {
    SchemaUpdatesTable schemaUpdatesTable = configuration.getSchemaUpdatesTable();
    final AtomicBoolean upToDate = new AtomicBoolean(false);
    while (!upToDate.get()) {
      tx(tx->{
          Set<String> tableNames = tx.getTableNames();
          if (!tableNames.contains(schemaUpdatesTable.getName())) {
            log.debug("Table " + schemaUpdatesTable.getName() + " does not exist");
            schemaUpdatesTable.create(tx);
          } else {
            log.debug("Table " + schemaUpdatesTable.getName() + " exists");
          }

          List<Table> tablesToCreate = configuration.getTables().stream()
            .filter(table->!tableNames.contains(table.getName()))
            .collect(Collectors.toList());

          List<String> performedSchemaUpdateNames = schemaUpdatesTable.findAllSchemaUpdateNames(tx);
          List<SchemaUpdate> notPerformedSchemaUpdates = configuration.getSchemaUpdates().stream()
            .filter(updateName->performedSchemaUpdateNames.contains(updateName.getName()))
            .collect(Collectors.toList());

          if (!tablesToCreate.isEmpty()
              || !notPerformedSchemaUpdates.isEmpty()) {

            String lockId = schemaUpdatesTable.acquireSchemaLock(tx);
            if (lockId!=null) {
              try {
                tablesToCreate.stream()
                  .forEach(table->tx.newCreateTable(table).execute());

                CreatedTablesList createdTablesList = new CreatedTablesList(tablesToCreate);
                notPerformedSchemaUpdates.forEach(schemaUpdate->{
                  schemaUpdate.getUpdate().accept(tx,createdTablesList);
                  schemaUpdatesTable.insertSchemaUpdateName(tx, schemaUpdate.getName());
                });
                log.debug("Schema updated");
                upToDate.set(true);
              } finally {
                schemaUpdatesTable.releaseSchemaLock(tx, lockId);
              }
            } else {
              // wait a while and try again
              try {
                Thread.sleep(5000);
              } catch (InterruptedException e) {
              }
            }
          } else {
            log.debug("Schema is up to date");
            upToDate.set(true);
          }
      });
    }
  }

  public void dropSchema() {
    tx(tx->{
      Set<String> tableNames = tx.getTableNames();
      configuration.getTables().forEach(table->{
        if (tableNames.contains(table.getName())) {
          tx.newDropTable(table)
            .execute();
        }
      });
      Table schemaUpdatesTable = configuration.getSchemaUpdatesTable();
      if (tableNames.contains(schemaUpdatesTable.getName())) {
        tx.newDropTable(schemaUpdatesTable)
          .execute();
      }
    });
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public DbConfiguration getConfiguration() {
    return configuration;
  }

  public void logSQL(Tx tx, String sql) {
    log.debug(tx.toString()+"\n\n"+sql+"\n");
  }

  public void logSqlParameter(Parameter parameter) {
    log.debug("["+parameter.index+"] "+parameter.formatValue());
  }
}

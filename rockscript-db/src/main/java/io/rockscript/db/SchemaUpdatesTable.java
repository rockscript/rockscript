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

import io.rockscript.db.columntypes.VarChar;

import java.util.*;

import static io.rockscript.db.WhereCondition.*;

public class SchemaUpdatesTable extends Table {

  public static final Column ID = new Column("id", new VarChar()).primaryKey();
  public static final Column NAME = new Column("name", new VarChar());
  public static final Column LOCK_ID = new Column("lock_id", new VarChar());

  private static final String NAME_DATABASE_LOCK = "database-lock";

  public SchemaUpdatesTable() {
    super("schema_updates",
      ID,
      NAME,
      LOCK_ID);
  }

  public void create(Tx tx) {
    tx.newCreateTable(this).execute();
    insertLockRow(tx);
  }

  private void insertLockRow(Tx tx) {
    tx.newInsert(this)
      .valueString(ID, tx.generateId())
      .valueString(NAME, NAME_DATABASE_LOCK)
      .execute()
      .assertInsertHappened();
  }

  public List<String> findAllSchemaUpdateNames(Tx tx) {
    List<String> schemaUpdateNames = new ArrayList<>();
    SelectResult selectResult = tx.newSelect()
      .from(this)
      .execute();
    while (selectResult.next()) {
      String updateName = selectResult.get(NAME);
      schemaUpdateNames.add(updateName);
    }
    return schemaUpdateNames;
  }

  /** returns the lockId that should be passed into {@link #releaseSchemaLock(Tx,String)} */
  public String acquireSchemaLock(Tx tx) {
    String lockId = generateLockId();
    int rowCount = tx.newUpdate(this)
      .setString(LOCK_ID, lockId)
      .where(and(equal(NAME, NAME_DATABASE_LOCK),
                 isNull(LOCK_ID)))
      .execute()
      .assertUpdateHappened()
      .getRowCount();
    return rowCount==1 ? lockId : null;
  }

  private String generateLockId() {
    return ""
      + new Random().nextInt(10)
      + new Random().nextInt(10)
      + new Random().nextInt(10)
      + new Random().nextInt(10);
  }

  public void insertSchemaUpdateName(Tx tx, String schemaUpdateName) {
    tx.newInsert(this)
      .valueString(NAME, schemaUpdateName)
      .execute()
      .assertInsertHappened();
  }

  /** returns true if the lock was released */
  public void releaseSchemaLock(Tx tx, String lockId) {
    tx
      .newUpdate(this)
      .setString(LOCK_ID, null)
      .where(and(equal(NAME, NAME_DATABASE_LOCK),
                 equal(LOCK_ID, lockId)))
      .execute()
      .assertUpdateHappened();
  }
}

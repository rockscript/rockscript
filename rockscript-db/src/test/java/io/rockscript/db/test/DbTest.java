/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript.db.test;

import io.rockscript.db.Column;
import io.rockscript.db.Db;
import io.rockscript.db.DbConfiguration;
import io.rockscript.db.Table;
import io.rockscript.db.columntypes.VarChar;
import io.rockscript.db.id.TestIdGenerator;
import io.rockscript.db.sqltypes.PostgresDb;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 */
public class DbTest {
  
  static Logger log = LoggerFactory.getLogger(DbTest.class);

  public static class TestTable extends Table {
    public TestTable() {
      super("test",
        new Column("id", new VarChar(1024)).primaryKey(),
        new Column("name", new VarChar(4096)));
    }
  }
  
  @Test
  public void testDb() {
    Db db = new DbConfiguration()
      .host("localhost")
      .databaseName("rockscript")
      .username("test")
      .password("test")
      .idGenerator(new TestIdGenerator())
      .dbType(new PostgresDb())
      .table(new TestTable())
      .build();

    db.updateSchema();
    db.dropSchema();
  }
}

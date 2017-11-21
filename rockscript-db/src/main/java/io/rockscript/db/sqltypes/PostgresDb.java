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
package io.rockscript.db.sqltypes;

import io.rockscript.db.SqlType;

public class PostgresDb implements SqlType {

  @Override
  public String getDriverClassName() {
    return "org.postgresql.Driver";
  }

  @Override
  public String getConnectionUrl(String server, Integer port, String databaseName) {
    return "jdbc:postgresql://"+server+(port!=null?":"+port:"")+"/"+databaseName;
  }

  @Override
  public String typeJson() {
    return "JSONB";
  }

//  @Override
//  public String getTemplateSelectAll() {
//    return "SELECT * FROM %s";
//  }
//
//  @Override
//  public String getTemplateDeleteAll() {
//    return "DELETE FROM %s";
//  }

  @Override
  public String typeVarchar(Integer n) {
    return (n!=null ? "VARCHAR("+n+")" : "VARCHAR");
  }

  @Override
  public String typeDateTime() {
    return "TIMESTAMP";
  }
}

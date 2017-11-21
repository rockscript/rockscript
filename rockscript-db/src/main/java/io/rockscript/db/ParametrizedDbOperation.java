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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


/**
 * @author Tom Baeyens
 */
public abstract class ParametrizedDbOperation {

  protected Tx tx;
  protected List<Parameter> parameters = null;

  public ParametrizedDbOperation(Tx tx) {
    this.tx = tx;
  }

  public String toString() {
    return toString(false);
  }

  public abstract String toString(boolean showParameterNames);

  protected Parameter setParameterString(String value) {
    return setParameter(new StringParameter(value));
  }

  protected Parameter setParameterOther(Object value) {
    return setParameter(new OtherParameter(value));
  }
  
  protected Parameter setParameter(Parameter parameter) {
    if (parameters==null) {
      parameters = new ArrayList<>();
    }
    parameters.add(parameter);
    parameter.setIndex(parameters.size());
    return parameter;
  }

  protected void setParameters(PreparedStatement preparedStatement) {
    parameters.stream().forEach(parameter->{
      tx.getDb().logSqlParameter(parameter);
      try {
        parameter.set(preparedStatement);
      } catch (SQLException e) {
        throw new DbException("Couldn't set parameter " + parameter.toString() + ": " + e.getMessage(), e);
      }
    });
  }
}

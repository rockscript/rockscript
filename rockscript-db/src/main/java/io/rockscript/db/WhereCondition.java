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
import java.util.stream.Collectors;

public abstract class WhereCondition {

  public abstract void setParameters(ParametrizedDbOperation operation);

  public String toString() {
    return toString(false);
  }

  public abstract String toString(boolean showParameterNames);

  public static WhereCondition equal(final Column column, final String value) {
    return new EqualCondition(column, value) {
      @Override
      public void setParameters(ParametrizedDbOperation operation) {
        this.parameter = operation.setParameterString(value);
      }
    };
  }

  static abstract class EqualCondition extends WhereCondition {
    Column column;
    Parameter parameter = null;
    String value;
    public EqualCondition(Column column, String value) {
      this.column = column;
      this.value = value;
    }
    @Override
    public String toString(boolean showParameterNames) {
      return column.getName()+" = "+(showParameterNames ? parameter.toString() : "?");
    }
  }

  public static WhereCondition and(final WhereCondition... andConditions) {
    return new WhereCondition() {
      @Override
      public void setParameters(ParametrizedDbOperation operation) {
        Arrays.stream(andConditions)
          .forEach(andCondition->andCondition.setParameters(operation));
      }
      @Override
      public String toString(boolean showParameterNames) {
        return "( " +
          Arrays.stream(andConditions)
            .map(andCondition->andCondition.toString(showParameterNames))
            .collect(Collectors.joining("\n        AND "))+
          " )";
      }
    };
  }

  public static WhereCondition isNull(final Column column) {
    return new WhereCondition() {
      @Override
      public void setParameters(ParametrizedDbOperation operation) {
      }
      @Override
      public String toString(boolean showParameterNames) {
        return column.getName()+" IS NULL";
      }
    };
  }
}

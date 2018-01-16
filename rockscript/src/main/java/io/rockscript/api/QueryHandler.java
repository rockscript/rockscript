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
package io.rockscript.api;

import io.rockscript.Engine;
import io.rockscript.http.servlet.*;
import io.rockscript.util.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class QueryHandler extends AbstractRequestHandler {

  static Logger log = LoggerFactory.getLogger(QueryHandler.class);

  Map<String,Class<? extends Query>> queryTypes = new HashMap<>();

  public QueryHandler() {
    super(GET, "/query/{queryName}");
  }

  public void setEngine(Engine engine) {
    super.setEngine(engine);

    engine
      .getQueries()
      .forEach(query->queryTypes.put(query.getName(), query.getClass()));
  }

  @Override
  public void handle(ServerRequest request, ServerResponse response) {
    try {
      Query query = parseQuery(request);
      Object queryResponse = query.execute(engine);

      response
        .bodyJson(queryResponse)
        .status(200);

    } catch (HttpException e) {
      throw e;
    } catch (Exception e) {
      log.debug("Couldn't execute query "+request.getQueryParameterMap()+": "+e.getMessage(), e);
      throw new InternalServerException();
    }
  }

  private Query parseQuery(ServerRequest request) {
    try {
      String queryName = request.getPathParameter("queryName");
      Class<? extends Query> queryClass = queryTypes.get(queryName);
      BadRequestException.throwIfNull(queryClass, "No query for q=%s. Expected one of %s", queryName, queryTypes.keySet());
      Query query = queryClass.newInstance();
      Map<String, String[]> parameterMap = request.getQueryParameterMap();
      for (String parameterName: parameterMap.keySet()) {
        if (!"q".equals(parameterName)) {
          Field field = Reflection.findFieldInClass(queryClass, parameterName);
          String[] parameterStringValues = (String[]) parameterMap.get(parameterName);
          Object fieldValue = convertParameterValuesToFieldType(parameterStringValues, field);
          if (fieldValue!=null) {
            field.setAccessible(true);
            field.set(query, fieldValue);
          }
        }
      }
      return query;
    } catch (Exception e) {
      throw new RuntimeException("Couldn't parse query: "+e.getMessage(), e);
    }
  }

  private Object convertParameterValuesToFieldType(String[] values, Field field) {
    Type fieldType = field.getGenericType();

    if (values==null || values.length==0) {
      return null;
    }

    if (fieldType == String.class) {
      return hasSingleValue(values) ? values[0] : null;

    } else if (fieldType == Boolean.class || fieldType == boolean.class) {
      if (values!=null) {
        if (values.length==0) {
          return true;
        }
        if (values.length==1 && isTrue(values[0])) {
          return true;
        }
      }
      return false;

    } else if (fieldType == Integer.class || fieldType == int.class) {
      if (hasSingleValue(values)) {
        return Integer.parseInt(values[0]);
      }
    }

    throw new BadRequestException("Couldn't parse query parameter "+field.getName()+" as a "+(fieldType instanceof Class ? ((Class)fieldType).getSimpleName() : fieldType.toString()));
  }

  private boolean hasSingleValue(String[] values) {
    return values!=null && values.length==1;
  }

  private static boolean isTrue(String value) {
    if (value==null) {
      return false;
    }
    String valueLower = value.toLowerCase();
    return "true".equals(valueLower)
           || "on".equals(valueLower)
           || "1".equals(valueLower)
           || "enabled".equals(valueLower)
           || "enable".equals(valueLower)
           || "ok".equals(valueLower)
           || "active".equals(valueLower);
  }
}

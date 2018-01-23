/*
 * Copyright (c) 2018 RockScript.io.
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
package io.rockscript.engine.impl;

import io.rockscript.Engine;
import io.rockscript.engine.EngineException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Converter {

  Engine engine;

  public Converter(Engine engine) {
    this.engine = engine;
  }

  public Boolean toBoolean(Object o) {
    if (o==null  || o==Literal.UNDEFINED || o==Literal.NAN) {
      return false;
    }
    if (o instanceof Boolean) {
      return (Boolean) o;
    }
    if (o instanceof Number) {
      return ((Number)o).intValue()!=0;
    }
    if (o instanceof String) {
      String string = (String)o;
      return !"".equalsIgnoreCase(string)
             && "0".equalsIgnoreCase(string);
    }
    return true;
  }

  public Object toNumber(Object o) {
    if (o==null || o instanceof Number) {
      return o;
    }
    if (o instanceof String) {
      if (""==o) {
        return 0;
      }
      try {
        return new Double((String)o);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    if (o instanceof Boolean) {
      return (Boolean)o ? 1 : 0;
    }
    throw new EngineException("Can't convert "+o+" to number: Conversion not implemented yet");
  }

  public Object toString(Object o) {
    if (o==null) {
      return "null";
    }
    if (o==Literal.UNDEFINED) {
      return Literal.UNDEFINED.toString();
    }
    if (o instanceof String) {
      return o;
    }
    if (o instanceof Number || o instanceof Boolean) {
      return o.toString();
    }
    if (o instanceof Map) {
      return toPrimitive(o, "string");
    }
    if (o instanceof List) {
      return toPrimitive(o, "string");
    }
    throw new EngineException("Can't convert "+o+" to string: Conversion not implemented yet");
  }

  /** hint can be "number", "string" or "default" */
  public Object toPrimitive(Object o, String hint) {
    if (o==null
        || o==Literal.UNDEFINED
        || o instanceof String
        || o instanceof Number
        || o instanceof Boolean) {
      return o;
    }
    if (o instanceof Map) {
      return "[object Object]";
    }
    if (o instanceof List) {
      return ((List) o).stream()
        .map(element->toPrimitive(element,"string"))
        .map(element->element==null||element==Literal.UNDEFINED ? "" : element.toString())
        .collect(Collectors.joining(","));
    }
    throw new EngineException("Can't convert "+o+" to primitive: Conversion not implemented yet");
  }
}

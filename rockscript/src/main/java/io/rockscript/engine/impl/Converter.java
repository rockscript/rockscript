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

  public static final String HINT_DEFAULT = "default";
  public static final String HINT_NUMBER = "number";
  public static final String HINT_STRING = "string";

  Engine engine;

  public Converter(Engine engine) {
    this.engine = engine;
  }

  public Boolean toBoolean(Object o) {
    if (isNull(o)  || isUndefined(o) || isNaN(o)) {
      return false;
    }
    if (isBoolean(o)) {
      return (Boolean) o;
    }
    if (isNumber(o)) {
      return ((Number)o).intValue()!=0;
    }
    if (isString(o)) {
      String string = (String)o;
      return !"".equalsIgnoreCase(string)
             && "0".equalsIgnoreCase(string);
    }
    return true;
  }

  public Number toNumber(Object o) {
    if (o==null || o instanceof Number) {
      return (Number) o;
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

  public String toString(Object o) {
    if (isNull(o)) {
      return "null";
    }
    if (isUndefined(o)) {
      return Literal.UNDEFINED.toString();
    }
    if (isString(o)) {
      return (String) o;
    }
    if (isNumber(o)) {
      return numberToString((Number)o);
    }
    if (isBoolean(o)) {
      return o.toString();
    }
    if (isObject(o)) {
      return (String) toPrimitive(o, "string");
    }
    if (isArray(o)) {
      return (String) toPrimitive(o, "string");
    }
    throw new EngineException("Can't convert "+o+" to string: Conversion not implemented yet");
  }

  protected String numberToString(Number number) {
    if (number instanceof Double && (Double)number%1==0) {
      return String.format("%.0f", number);
    }
    return number.toString();
  }

  public Object toPrimitiveDefault(Object o) {
    return toPrimitive(o, HINT_DEFAULT);
  }

  public Object toPrimitiveNumber(Object o) {
    return toPrimitive(o, HINT_NUMBER);
  }

  public Object toPrimitiveString(Object o) {
    return toPrimitive(o, HINT_STRING);
  }

  /** hint can be "number", "string" or "default" */
  public Object toPrimitive(Object o, String hint) {
    if (isNull(o)) {
      if (HINT_DEFAULT.equals(hint)  || HINT_STRING.equals(hint)) {
        return "";
      } else {
        return 0d;
      }
    }
    if (isNumber(o)
        || isUndefined(o)
        || isString(o)
        || isNumber(o)
        || isBoolean(o)) {
      return o;
    }
    if (o instanceof Map) {
      return "[object Object]";
    }
    if (o instanceof List) {
      return ((List) o).stream()
        .map(element->toPrimitive(element,"string"))
        .map(element->isNull(element)||isUndefined(element) ? "" : toString(element))
        .collect(Collectors.joining(","));
    }
    throw new EngineException("Can't convert "+o+" to primitive: Conversion not implemented yet");
  }

  /** does the value o represent a javascript null */
  public static boolean isNull(Object o) {
    return o == null;
  }

  /** does the value o represent a javascript object */
  public static boolean isObject(Object o) {
    return o instanceof Map;
  }

  /** does the value o represent a javascript string */
  public static boolean isString(Object o) {
    return o instanceof String;
  }

  /** does the value o represent a javascript number */
  public static boolean isNumber(Object o) {
    return o instanceof Number;
  }

  /** does the value o represent a javascript boolean */
  public static boolean isBoolean(Object o) {
    return o instanceof Boolean;
  }

  /** does the value o represent a javascript array */
  public static boolean isArray(Object o) {
    return o instanceof List;
  }

  /** does the value o represent a javascript undefined value */
  public static boolean isUndefined(Object o) {
    return o == Literal.UNDEFINED;
  }

  /** does the value o represent a javascript NaN (not a number) */
  public static boolean isNaN(Object o) {
    return o == Literal.NAN;
  }
}

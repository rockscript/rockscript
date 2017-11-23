/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
 *
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
package io.rockscript.util;

import io.rockscript.engine.EngineException;

import java.lang.reflect.Field;

public class Reflection {

  public static Field findFieldInObject(Object target, String fieldName) {
    if (target==null || fieldName==null) {
      return null;
    }
    return findFieldInClass(target.getClass(), fieldName);
  }

  public static Field findFieldInClass(Class<?> clazz, String fieldName) {
    if (clazz==null || fieldName==null) {
      return null;
    }
    for (Field field: clazz.getDeclaredFields()) {
      if (fieldName.equals(field.getName())) {
        return field;
      }
    }
    Class<?> superclass = clazz.getSuperclass();
    if (superclass!=null) {
      return findFieldInClass(superclass, fieldName);
    }
    return null;
  }


  public static Object getFieldValue(Field field, Object target) {
    try {
      field.setAccessible(true);
      return field.get(target);
    } catch (IllegalAccessException e) {
      throw new EngineException("Couldn't get field value with reflection: "+e.getMessage(), e);
    }
  }
}

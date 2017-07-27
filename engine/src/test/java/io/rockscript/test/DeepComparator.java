/*
 * Copyright ©2017, RockScript.io. All rights reserved.
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
 *
 */

package io.rockscript.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeepComparator {

  static Logger log = LoggerFactory.getLogger(DeepComparator.class);

  public static final Set<Class<?>> VALUE_CLASSES = new HashSet<>(Arrays.asList(
    String.class,
    Number.class
  ));

  Stack<String> path = new Stack<>();
  Map<Object,Object> comparing = new HashMap<>();
  Set<Field> ignoredFields = new HashSet<>();
  Map<String, List<Class<?>>> ignoredAnonymousClasses = new HashMap<>();
  StringBuffer logs = new StringBuffer();
  boolean error = false;

  public DeepComparator ignoreField(Class clazz, String fieldName) {
    try {
      Field field = clazz.getDeclaredField(fieldName);
      ignoredFields.add(field);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException("Field "+fieldName+" doesn't exist", e);
    }
    return this;
  }

  public DeepComparator ignoreAnonymousField(Class clazz, String fieldName) {
    List<Class<?>> classes = ignoredAnonymousClasses.get(fieldName);
    if (classes==null) {
      classes = new ArrayList<>();
      ignoredAnonymousClasses.put(fieldName, classes);
    }
    classes.add(clazz);
    return this;
  }

  public void assertEquals(Object a, Object b) {
    assertEqualsNoThrow(a, b);
    if (error) {
      throw new AssertionError("Objects are not deeply equal: \n"+logs.toString());
    }
  }

  public void assertEqualsNoThrow(Object a, Object b) {
    if (a==null || b==null) {
      if (a!=null || b!=null) {
        fail(a, b);
      } else {
        ok();
      }
    } else {
      if (!alreadyComparing(a, b)) {
        comparing.put(a, b);
        if (!a.getClass().equals(b.getClass())) {
          fail(a, b);

        } else if (isValueClass(a.getClass())) {
          if (!a.equals(b)) {
            fail(a, b);
          } else {
            ok();
          }
        } else if (Map.class.isAssignableFrom(a.getClass())) {
          assertMapEquals(a, b);
        } else if (isCollectionClass(a.getClass())) {
          assertCollectionEquals(a, b);
        } else {
          assertFieldsEqual(a.getClass(), a, b);
        }
      }
    }
  }

  private boolean alreadyComparing(Object a, Object b) {
    Object alreadyBeingChecked = comparing.get(a);
    return b==alreadyBeingChecked;
  }

  private void assertFieldsEqual(Class<?> clazz, Object a, Object b) {
    Field[] fields = clazz.getDeclaredFields();
    if (fields!=null) {
      for (Field field: fields) {
        if (!Modifier.isStatic(field.getModifiers())
            && !ignoredField(field)
            && !ignoredAnonymousField(field)) {
          path.push(field.getName());
          field.setAccessible(true);
          try {
            assertEqualsNoThrow(field.get(a), field.get(b));
          } catch (IllegalAccessException e) {
            throw new RuntimeException(path.toString(), e);
          }
          path.pop();
        }
      }
    }
    Class<?> superclass = clazz.getSuperclass();
    if (superclass!=null && Object.class!=null) {
      assertFieldsEqual(superclass, a, b);
    }
  }

  private boolean ignoredField(Field field) {
    return ignoredFields.contains(field);
  }

  // @SuppressWarnings("unchecked")
  private boolean ignoredAnonymousField(Field field) {
    List<Class<?>> classes = (List<Class<?>>)ignoredAnonymousClasses.get(field.getName());
    if (classes!=null) {
      for (Class<?> clazz: classes) {
        if (clazz.isAssignableFrom((Class<?>)field.getDeclaringClass())) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isValueClass(Class<?> clazz) {
    return VALUE_CLASSES.stream()
      .anyMatch(element->element.isAssignableFrom(clazz))
      || clazz.isEnum();
  }

  private boolean isCollectionClass(Class<?> clazz) {
    return Collection.class.isAssignableFrom(clazz);
  }

  @SuppressWarnings("unchecked")
  private void assertCollectionEquals(Object a, Object b) {
    List<Object> collectionA = new ArrayList<Object>((Collection<Object>) a);
    List<Object> collectionB = new ArrayList<Object>((Collection<Object>) b);
    if (collectionB.size()!=collectionA.size()) {
      path.push("size");
      fail(collectionA.size(), collectionB.size());
      path.pop();
    }
    int maxSize = Math.max(collectionA.size(), collectionB.size());
    for (int i=0; i<maxSize; i++) {
      path.push("["+i+"]");
      Object elementA = collectionA.size()>i ? collectionA.get(i) : "collection size mismatch";
      Object elementB = collectionB.size()>i ? collectionB.get(i) : "collection size mismatch";
      assertEqualsNoThrow(elementA, elementB);
      path.pop();
    }
  }

  @SuppressWarnings("unchecked")
  private void assertMapEquals(Object a, Object b) {
    Map<Object, Object> mapA = new HashMap<Object, Object>((Map<Object, Object>) a);
    Map<Object, Object> mapB = new HashMap<Object, Object>((Map<Object, Object>) b);
    if (mapA.size()!=mapB.size()) {
      throw new RuntimeException("Maps not same size: "+mapA.size()+"!="+mapB.size()+" <- "+path.toString());
    }
    for (Object key: mapA.keySet()) {
      path.push("['"+key+"']");
      assertEqualsNoThrow(mapA.get(key), mapB.get(key));
      path.pop();
    }
  }

  private void ok() {
    logs.append("== "+getPathString()+"\n");
  }

  private void fail(Object a, Object b) {
    error = true;
    logs.append("ERROR "+getPathString()+" : "+toString(a)+" != "+toString(b)+"\n");
  }

  private String toString(Object o) {
    return o==null ? "null" : o.toString()+" ("+o.getClass().getSimpleName()+")";
  }

  public String getPathString() {
    return path.stream().collect(Collectors.joining("."));
  }
}

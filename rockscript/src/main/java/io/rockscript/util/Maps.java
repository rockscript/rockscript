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
 */
package io.rockscript.util;

import java.util.*;

/** Usage:
 *
 * hashMap(
 *   entry("2360", "Oud-Turnhout");
 *   entry("3021 HN", "Rotterdam");
 * ); */
public class Maps {

  public static class Entry<K,V> {
    K key;
    V value;
    public Entry(K key, V value) {
      this.key = key;
      this.value = value;
    }
  }

  @SafeVarargs
  public static <K,V> HashMap<K,V> hashMap(Entry<K,V>... entries) {
    return buildMap(new HashMap<K,V>(), entries);
  }

  @SafeVarargs
  public static <K,V> LinkedHashMap<K,V> linkedHashMap(Entry<K,V>... entries) {
    return buildMap(new LinkedHashMap<K,V>(), entries);
  }

  @SafeVarargs
  static <K, V, M extends Map<K,V>> M buildMap(M map, Entry<K,V>... entries) {
    if (entries!=null) {
      for (Entry<K,V> entry: entries) {
        map.put(entry.key, entry.value);
      }
    }
    return map;
  }

  public static <K,V> Entry<K,V> entry(K key, V value) {
    return new Entry<K,V>(key, value);
  }
}

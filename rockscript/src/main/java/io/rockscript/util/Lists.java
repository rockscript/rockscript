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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lists {

  @SafeVarargs
  public static <T> List<T> of(T... elements) {
    return Arrays.asList(elements);
  }

  public static <T> List<T> arrayList(T... elements) {
    return new ArrayList(of(elements));
  }

  public static <T> T removeLast(List<T> list) {
    return list.remove(list.size()-1);
  }

  /** the last element or null if the list is null or empty */
  public static <T> T getLast(List<T> list) {
    return list!=null && !list.isEmpty() ? list.get(list.size()-1) : null;
  }

  /** replaces the last element with the given element if the list
   * is not null and not empty.  The method returns without exception
   * and without effect if the list is null or empty */
  public static <T> T setLast(List<T> list, T element) {
    if (list!=null && !list.isEmpty()) {
      return list.set(list.size()-1, element);
    }
    return null;
  }
}

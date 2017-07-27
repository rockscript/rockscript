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
package io.rockscript.netty.router;

import java.util.HashMap;
import java.util.Map;

public class Context {

  protected Map<String,Object> objects = new HashMap<>();

  public void set(Object o) {
    if (o!=null) {
      objects.put(o.getClass().getName(), o);
    }
  }

  public void set(String name, Object o) {
    objects.put(name, o);
  }

  public <T> T get(Class<T> clazz) {
    return get(clazz!=null ? clazz.getName() : null);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String name) {
    return (T) objects.get(name);
  }
}

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
package io.rockscript.gson;

import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.reflect.TypeToken;

public class PolymorphicTypeResolver {

  /** actual type tokens as the user provided them (with generic
   * type args resolved), mapped by their raw class */
  Map<Class<?>, TypeToken<?>> actualTypeTokens = new HashMap<>();

  public PolymorphicTypeResolver(Set<TypeToken<?>> typeTokens) {
    for (TypeToken<?> typeToken: typeTokens) {
      add(typeToken);
    }
  }

  public void add(TypeToken<?> typeToken) {
    Class<?> rawType = typeToken.getRawType();
    this.actualTypeTokens.put(rawType, typeToken);
    Type genericSuperclass = rawType.getGenericSuperclass();
    if (genericSuperclass!=null) {
      add(TypeToken.get(genericSuperclass));
    }
  }

  public TypeToken<?> resolve(Type type) {
    if (type==null) {
      return null;
    }
    Class<?> rawClass = TypeToken
        .get(type)
        .getRawType();
    return actualTypeTokens.get(rawClass);
  }
}

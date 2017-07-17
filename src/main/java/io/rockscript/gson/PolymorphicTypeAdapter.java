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

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.*;

public class PolymorphicTypeAdapter<T> extends TypeAdapter<T> {

  Map<String,PolymorphicTypeFields> polymorphicTypesByName = new HashMap<>();
  Map<Class<?>, PolymorphicTypeFields> polymorphicTypesByRawClass = new HashMap<>();
  Map<String,Class<?>> typesByName = new HashMap<>();
  PolymorphicTypeNameStrategy typeNameStrategy;

  public PolymorphicTypeAdapter(TypeToken<T> declaredBaseType, PolymorphicTypeAdapterFactory factory, Gson gson) {
    typeNameStrategy = factory.typeNameStrategy;
    Set<TypeToken<?>> types = factory.typeNames.keySet();
    PolymorphicTypeResolver typeResolver = new PolymorphicTypeResolver(types);
    for (TypeToken<?> type: types) {
      String typeName = factory.typeNames.get(type);
      PolymorphicTypeFields polymorphicTypeFields = new PolymorphicTypeFields(typeName, type, typeResolver, gson);
      polymorphicTypesByName.put(typeName, polymorphicTypeFields);
      Class<?> rawClass = type.getRawType();
      polymorphicTypesByRawClass.put(rawClass, polymorphicTypeFields);
      typesByName.put(typeName, rawClass);
    }
  }

  /** creates a map that maps generic type argument names to type tokens */
  private static Map<String, TypeToken> getActualTypeArguments(TypeToken<?> typeToken) {
    Class<?> rawClass = typeToken.getRawType();
    Type type = typeToken.getType();
    TypeVariable<? extends Class<?>>[] typeParameters = rawClass.getTypeParameters();
    if (typeParameters==null || !(type instanceof ParameterizedType)) {
      return null;
    }
    Map<String, TypeToken> genericTypes = new HashMap<>();
    ParameterizedType parameterizedType = (ParameterizedType) type;
    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
    for (int i=0; i<typeParameters.length; i++) {
      String typeParameterName = typeParameters[i].getName();
      TypeToken<?> actualType = TypeToken.get(actualTypeArguments[i]);
      genericTypes.put(typeParameterName, actualType);
    }
    return genericTypes;
  }

  @Override
  public void write(JsonWriter out, T value) throws IOException {
    if (value!=null) {
      PolymorphicTypeFields polymorphicTypeFields = polymorphicTypesByRawClass.get(value.getClass());
      String typeName = polymorphicTypeFields.typeName;
      try {
        typeNameStrategy.write(out, typeName, new FieldsWriter(out, value, this));
      } catch (IOException e) {
        throw e;
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      out.nullValue();
    }
  }

  @Override
  public T read(JsonReader in) throws IOException {
    if (in.peek()== JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    try {
      return (T) typeNameStrategy.read(in, new FieldsReader(in, this));
    } catch (IOException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}

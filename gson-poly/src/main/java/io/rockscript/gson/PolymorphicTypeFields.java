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

import java.lang.reflect.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.*;

public class PolymorphicTypeFields {

  String typeName;
  Map<String,PolymorphicField> polymorphicFields = new LinkedHashMap<>();

  /**
   * @param typeName the unique name used in json serialization to identify the given type.
   * @param type is the type for which this PolymorphicTypeFields contains the field serializations
   * @param typeResolver is the list of all actual types in the inheritance tree
   * @param gson is the overall Gson object from which field adapters can be looked up         */
  public PolymorphicTypeFields(String typeName, TypeToken<?> type, PolymorphicTypeResolver typeResolver, Gson gson) {
    this.typeName = typeName;
    // loops over all types in the inheritance chain to collects all fields
    // for the given type in a single list.  This is the list of fields that
    // will be used later when serializing and deserializing objects of the given type.
    List<TypeToken> inheritanceTypes = getInheritanceTypes(type, typeResolver);
    for (TypeToken inheritanceType: inheritanceTypes) {
      Map<String,Type> actualTypeArguments = getActualTypeArguments(inheritanceType);
      scanFields(inheritanceType, actualTypeArguments, gson);
    }
  }

  /** creates a map that maps generic type argument names to type tokens */
  private static Map<String, Type> getActualTypeArguments(TypeToken<?> typeToken) {
    Class<?> rawClass = typeToken.getRawType();
    Type type = typeToken.getType();
    TypeVariable<? extends Class<?>>[] typeParameters = rawClass.getTypeParameters();
    if (typeParameters==null || !(type instanceof ParameterizedType)) {
      return null;
    }
    Map<String, Type> genericTypes = new HashMap<>();
    ParameterizedType parameterizedType = (ParameterizedType) type;
    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
    for (int i=0; i<typeParameters.length; i++) {
      String typeParameterName = typeParameters[i].getName();
      Type actualType = actualTypeArguments[i];
      genericTypes.put(typeParameterName, actualType);
    }
    return genericTypes;
  }

  /** adds all declared fields in the given inheritanceType to the polymorphicFields member field */
  private void scanFields(TypeToken inheritanceType, Map<String, Type> actualTypeArguments, Gson gson) {
    Class rawClass = inheritanceType.getRawType();
    for (Field field: rawClass.getDeclaredFields()) {
      Type fieldType = field.getGenericType();
      Type concreteFieldType = concretize(fieldType, actualTypeArguments);
      TypeToken<?> concreteFieldTypeToken = TypeToken.get(concreteFieldType);
      TypeAdapter<?> fieldTypeAdapter = gson.getAdapter(concreteFieldTypeToken);
      @SuppressWarnings("unchecked")
      PolymorphicField polymorphicField = new PolymorphicField(field, fieldTypeAdapter);
      polymorphicFields.put(field.getName(), polymorphicField);
    }
  }

  /** resolves generic type arguments in field types */
  private Type concretize(Type fieldType, Map<String, Type> actualTypeArguments) {
    if (fieldType instanceof TypeVariable) {
      TypeVariable fieldTypeVariable = (TypeVariable) fieldType;
      String genericTypeVariableName = fieldTypeVariable.getName();
      Type resolvedType = actualTypeArguments.get(genericTypeVariableName);
      if (resolvedType==null) {
        throw new RuntimeException("Couldn't resolve type variable "+fieldType+" with "+actualTypeArguments);
      }
      return resolvedType;
    } else if (fieldType instanceof ParameterizedType) {
      ParameterizedType parameterizedFieldType = (ParameterizedType) fieldType;
      boolean anyParameterHasGenerics = false;
      Type[] fieldTypeArguments = parameterizedFieldType.getActualTypeArguments();
      int numberOfParameters = fieldTypeArguments.length;
      Type[] actualTypeParameters = new Type[numberOfParameters];
      for (int i=0; i<numberOfParameters; i++) {
        actualTypeParameters[i] = concretize(fieldTypeArguments[i], actualTypeArguments);
        boolean parameterHasGenerics = fieldTypeArguments[i] != actualTypeParameters[i];
        anyParameterHasGenerics = anyParameterHasGenerics || parameterHasGenerics;
      }
      if (anyParameterHasGenerics) {
        Type rawType = parameterizedFieldType.getRawType();
        return new ParameterizedTypeImpl(null, rawType, actualTypeParameters);
      }
    }
    return fieldType;
  }

  /** list of types in the full inheritance chain of the given type, ordered from root base
   * type first, to most specific subtype (the given type) last.
   * This is used in the ordering of fields in the serialized json. */
  private List<TypeToken> getInheritanceTypes(TypeToken<?> type, PolymorphicTypeResolver typeResolver) {
    List<TypeToken> inheritenceTypes = new ArrayList<>();
    while (type!=null) {
      inheritenceTypes.add(0, type);
      Type superType = type.getRawType().getGenericSuperclass();
      type = typeResolver.resolve(superType);
    }
    return inheritenceTypes;
  }

  public void read(JsonReader in, Object bean) throws Exception {
    while (in.peek() == JsonToken.NAME) {
      String fieldName = in.nextName();
      PolymorphicField field = polymorphicFields.get(fieldName);
      // ignore non-existing fields
      if (field!=null) {
        field.read(in, bean);
      }
    }
  }

  public void write(JsonWriter out, Object bean) throws Exception {
    for (String fieldName: polymorphicFields.keySet()) {
      PolymorphicField field = polymorphicFields.get(fieldName);
      out.name(fieldName);
      field.write(out, bean);
    }
  }

  /** knows how to serialize and deserialize a field in a polymorphic type */
  public static class PolymorphicField<T> {
    Field field;
    TypeAdapter<T> fieldTypeAdapter;
    public PolymorphicField(Field field, TypeAdapter<T> fieldTypeAdapter) {
      this.field = field;
      field.setAccessible(true);
      this.fieldTypeAdapter = fieldTypeAdapter;
    }
    public void read(JsonReader in, Object bean) throws Exception {
      Object fieldValue = fieldTypeAdapter.read(in);
      field.set(bean, fieldValue);
    }
    public void write(JsonWriter out, Object bean) throws Exception {
      @SuppressWarnings("unchecked")
      T fieldValue = (T) field.get(bean);
      fieldTypeAdapter.write(out, fieldValue);
    }
  }
}

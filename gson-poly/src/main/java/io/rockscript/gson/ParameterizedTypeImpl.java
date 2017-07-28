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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import static com.google.gson.internal.$Gson$Types.canonicalize;
import static com.google.gson.internal.$Gson$Types.typeToString;

public class ParameterizedTypeImpl implements ParameterizedType, Serializable {

  private static final long serialVersionUID = 0;

  private final Type ownerType;
  private final Type rawType;
  private final Type[] typeArguments;

  public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
    // require an owner type if the raw type needs it
    ensureOwnerType(ownerType, rawType);

    this.ownerType = ownerType == null ? null : canonicalize(ownerType);
    this.rawType = canonicalize(rawType);
    this.typeArguments = typeArguments.clone();
    for (int t = 0; t < this.typeArguments.length; t++) {
      checkNotNull(this.typeArguments[t], "type parameter");
      checkNotPrimitive(this.typeArguments[t], "type parameters");
      this.typeArguments[t] = canonicalize(this.typeArguments[t]);
    }
  }

  public Type[] getActualTypeArguments() {
    return typeArguments.clone();
  }

  public Type getRawType() {
    return rawType;
  }

  public Type getOwnerType() {
    return ownerType;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ParameterizedType)) {
      return false;
    }
    if (this==other) {
      // also handles (a == null && b == null)
      return true;
    }
    if (!(other instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType pb = (ParameterizedType) other;
    return objectsEqual(this.getOwnerType(), pb.getOwnerType())
           && this.getRawType().equals(pb.getRawType())
           && Arrays.equals(this.getActualTypeArguments(), pb.getActualTypeArguments());
  }

  private boolean objectsEqual(Object a, Object b) {
    return a == b || (a != null && a.equals(b));
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(typeArguments)
           ^rawType.hashCode()
           ^hashCodeOrZero(ownerType);
  }

  private static int hashCodeOrZero(Object o) {
    return o != null ? o.hashCode() : 0;
  }

  @Override public String toString() {
    StringBuilder stringBuilder = new StringBuilder(30 * (typeArguments.length + 1));
    stringBuilder.append(typeToString(rawType));

    if (typeArguments.length == 0) {
      return stringBuilder.toString();
    }

    stringBuilder.append("<").append(typeToString(typeArguments[0]));
    for (int i = 1; i < typeArguments.length; i++) {
      stringBuilder.append(", ").append(typeToString(typeArguments[i]));
    }
    return stringBuilder.append(">").toString();
  }

  private static void ensureOwnerType(Type ownerType, Type rawType) {
    if (rawType instanceof Class<?>) {
      Class rawTypeAsClass = (Class) rawType;
      checkArgument(ownerType != null || rawTypeAsClass.getEnclosingClass() == null,
                    "No owner type for enclosed %s", rawType);
      checkArgument(ownerType == null || rawTypeAsClass.getEnclosingClass() != null,
                    "Owner type for unenclosed %s", rawType);
    }
  }

  private static void checkNotPrimitive(Type type, String use) {
    checkArgument(!(type instanceof Class<?>) || !((Class) type).isPrimitive(),
                  "Primitive types are not allowed in %s: %s", use, type);
  }

  private static void checkArgument(boolean expression,
    String errorMessageTemplate,
    Object... errorMessageArgs) {
    if (!expression) {
      throw new IllegalArgumentException(String.format(errorMessageTemplate, errorMessageArgs));
    }
  }

  private static <T> T checkNotNull(T reference, Object errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }
}

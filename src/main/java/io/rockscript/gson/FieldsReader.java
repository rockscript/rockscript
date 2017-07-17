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

import com.google.gson.stream.JsonReader;

public class FieldsReader {

  JsonReader in;
  PolymorphicTypeAdapter<?> typeAdapter;
  PolymorphicTypeFields polymorphicTypeFields;

  public FieldsReader(JsonReader in, PolymorphicTypeAdapter<?> typeAdapter) {
    this.in = in;
    this.typeAdapter = typeAdapter;
  }

  public Object instantiateBean(String typeName) {
    Class<?> concreteType = typeAdapter.typesByName.get(typeName);
    if (concreteType==null) {
      throw new RuntimeException("No concrete type for "+typeName);
    }
    polymorphicTypeFields = typeAdapter.polymorphicTypesByName.get(typeName);
    try {
      return concreteType.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate new "+concreteType+": "+e.getMessage(), e);
    }
  }

  public void readFields(Object bean) throws Exception {
    polymorphicTypeFields.read(in, bean);
  }
}

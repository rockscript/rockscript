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
import com.google.gson.stream.JsonWriter;

/** represents how the type information is encoded in the JSON
 *
 * WRAPPER_OBJECT -> { "circle" : { "radius": 10 }}
 * typical_but_inferior_type_name_strategy -> { "type": "circle", "radius": 10 }
 */
public interface PolymorphicTypeNameStrategy {

  /** { "circle" : { "radius": 10 }} */
  PolymorphicTypeNameStrategy WRAPPER_OBJECT = new PolymorphicTypeNameStrategy() {
    @Override
    public Object read(JsonReader in, FieldsReader fieldsReader) throws Exception{
      in.beginObject();
      String typeName = in.nextName();
      Object bean = fieldsReader.instantiateBean(typeName);
      in.beginObject();
      fieldsReader.readFields(bean);
      in.endObject();
      in.endObject();
      return bean;
    }
    @Override
    public void write(JsonWriter out, String typeName, FieldsWriter fieldsWriter) throws Exception {
      out.beginObject();
      out.name(typeName);
      out.beginObject();
      fieldsWriter.writeFields(typeName);
      out.endObject();
      out.endObject();
    }
  };

  Object read(JsonReader in, FieldsReader fieldsReader) throws Exception;

  void write(JsonWriter out, String typeName, FieldsWriter fieldsWriter) throws Exception;
}

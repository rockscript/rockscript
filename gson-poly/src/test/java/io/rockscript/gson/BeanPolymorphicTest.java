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

/*
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import static io.rockscript.gson.JsonQuotes.quote;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BeanPolymorphicTest {

  static Gson gson = new GsonBuilder().registerTypeAdapterFactory(new PolymorphicTypeAdapterFactory()
    .typeName(new TypeToken<Shape>(){}, "shape")
    .typeName(new TypeToken<Square>(){}, "square")
    .typeName(new TypeToken<Circle>(){}, "circle"))
  .create();

  public static class Shape {

    String color;
  }

  public static class Square extends Shape {

    int side;
  }

  public static class Circle extends Shape {

    int radius;
  }

  @Test
  public void testPolymorphicSpecificClassReadAsBaseClass() {
    String originalJson = quote("{'circle':{"+"'color':'green',"+"'radius':5}}");
    Type type = new TypeToken<Shape>() {

    }.getType();

    Circle circle = gson.fromJson(originalJson, type);

    assertNotNull(circle);
    assertEquals("green", circle.color);
    assertEquals(5, circle.radius);

    String reserializedJson = gson.toJson(circle);
    assertEquals(originalJson, reserializedJson);
  }

  @Test
  public void testPolymorphicSpecificClass() {
    String originalJson = quote("{'circle':{"+"'color':'green',"+"'radius':5}}");
    Type type = new TypeToken<Circle>() {

    }.getType();

    Circle circle = gson.fromJson(originalJson, type);

    assertNotNull(circle);
    assertEquals("green", circle.color);
    assertEquals(5, circle.radius);

    String reserializedJson = gson.toJson(circle);
    assertEquals(originalJson, reserializedJson);
  }

  @Test
  public void testPolymorphicBaseClass() {
    String originalJson = quote("{'shape':{"+"'color':'green'}}");
    Type type = new TypeToken<Shape>() {

    }.getType();

    Shape shape = gson.fromJson(originalJson, type);

    assertNotNull(shape);
    assertEquals("green", shape.color);

    String reserializedJson = gson.toJson(shape);
    assertEquals(originalJson, reserializedJson);
  }

  //  @Test
  //  public void testPolymorphicNonExistingFieldInBaseClass() {
  //    String originalJson = JsonQuotes.quote(
  //        "{'shape':{" +
  //            "'color':'green'," +
  //            "'nonexisting':'hello'}}");
  //    Type type = new TypeToken<Shape>(){}.getType();
  //
  //    Shape shape = gson.fromJson(originalJson, type);
  //
  //    assertNotNull(shape);
  //    assertEquals("green", shape.color);
  //
  //    String reserializedJson = gson.toJson(shape);
  //    assertEquals("{'shape':{" +
  //        "'color':'green'}}", reserializedJson);
  //  }
}

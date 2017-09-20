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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GsonPolyGenericsTest {

  static Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new PolymorphicTypeAdapterFactory()
          .typeName(new TypeToken<GenericShape<Integer>>(){}, "shape")
          .typeName(new TypeToken<GenericCircle<Integer,String>>(){}, "circle"))
      .create();

  public static class GenericShape<C> {
    C color;
  }

  public static class GenericCircle<X,R> extends GenericShape<X> {
    R radius;
  }

  @Ignore // TODO Fix testPolymorphicGenericReadCircle
  @Test
  public void testPolymorphicGenericReadCircle() {
    testCircle(new TypeToken<GenericCircle<Integer, String>>() {}.getType());
    testCircle(new TypeToken<GenericShape<Integer>>() {}.getType());
    testCircle(new TypeToken<GenericCircle>() {}.getType());
    testCircle(new TypeToken<GenericShape>() {}.getType());
  }

  public void testCircle(Type type) {
    String originalJson = JsonQuotes.quote(
        "{'circle':{" +
          "'color':1," +
          "'radius':'2 meters'" +
        "}}");

    GenericCircle<Integer,String> circle = gson.fromJson(originalJson, type);

    assertNotNull(circle);
    assertEquals(1, (int)circle.color);
    assertEquals("2 meters", (String)circle.radius);

    String reserializedJson = gson.toJson(circle);
    assertEquals(originalJson, reserializedJson);

  }

  @Test
  public void testPolymorphicGenericShape() {
    testShape(new TypeToken<GenericShape<Integer>>() {}.getType());
    testShape(new TypeToken<GenericShape>() {}.getType());
  }

  public void testShape(Type type) {
    String originalJson = JsonQuotes.quote(
        "{'shape':{" +
          "'color':1" +
        "}}");

    GenericShape<Integer> shape = gson.fromJson(originalJson, type);

    assertNotNull(shape);
    assertEquals(1, (int)shape.color);

    String reserializedJson = gson.toJson(shape);
    assertEquals(originalJson, reserializedJson);
  }
}

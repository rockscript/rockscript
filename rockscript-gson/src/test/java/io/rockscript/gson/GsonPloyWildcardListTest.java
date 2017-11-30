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
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GsonPloyWildcardListTest {

  static Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new PolymorphicTypeAdapterFactory()
          .typeName(new TypeToken<Color>(){}, "color")
          .typeName(new TypeToken<RgBColor>(){}, "rgbColor"))
      .create();

  public static class Shape {
    List<? extends Color> colors;
  }

  public static class Color {
  }

  public static class RgBColor extends Color {
    int r;
    int g;
    int b;
  }

  @Test
  public void testWildcard() {
    String originalJson = JsonQuotes.quote(
        "{'colors':[{'rgbColor':{" +
         "'r':1," +
         "'g':2," +
         "'b':3" +
         "}}]}");
    Type type = new TypeToken<Shape>() {}.getType();

    Shape shape = gson.fromJson(originalJson, type);

    assertNotNull(shape);
    assertEquals(1, (int) ((RgBColor)shape.colors.get(0)).r);
    assertEquals(2, (int) ((RgBColor)shape.colors.get(0)).g);
    assertEquals(3, (int) ((RgBColor)shape.colors.get(0)).b);

    String reserializedJson = gson.toJson(shape);
    assertEquals(originalJson, reserializedJson);
  }
}

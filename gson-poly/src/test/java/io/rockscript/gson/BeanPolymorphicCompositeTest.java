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
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BeanPolymorphicCompositeTest {

  static Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new PolymorphicTypeAdapterFactory()
          .typeName(new TypeToken<Identity>(){}, "identity")
          .typeName(new TypeToken<User>(){}, "user")
          .typeName(new TypeToken<Group>(){}, "group"))
      .create();

  public static class Identity {
    String name;
  }

  public static class User extends Identity {
  }

  public static class Group extends Identity {
    List<Identity> members;
  }

  @Test
  public void testPolymorphicCompositeTestRead() {
    String originalJson = JsonQuotes.quote(
        "{'group':{" +
          "'name':'company'," +
          "'members':[" +
            "{'group':{" +
              "'name':'sales'," +
              "'members':[" +
                "{'user':{" +
                 "'name':'john'}}]}}," +
            "{'user':{" +
             "'name':'mary'}}]" +
        "}}");
    Type type = new TypeToken<Identity>() {}.getType();

    Group company = gson.fromJson(originalJson, type);

    assertNotNull(company);
    assertEquals("company", company.name);
    Group sales = (Group) company.members.get(0);
    assertEquals("sales", sales.name);
    User john = (User) sales.members.get(0);
    assertEquals("john", john.name);
    User mary= (User) company.members.get(1);
    assertEquals("mary", mary.name);

    String reserializedJson = gson.toJson(company);
    assertEquals(originalJson, reserializedJson);
  }
}

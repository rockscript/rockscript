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
package io.rockscript.activity;

import io.rockscript.engine.JsonObject;

/** Special JsonObject used as import object that ensures automatic
 * capturing of the propertyName of Activity values and wraps the
 * activities so that the toString shows the property name. */
public class ImportJsonObject extends JsonObject {

  public void resolveActivityNames(String url) {
    for (String propertyName: getPropertyNames()) {
      Object value = get(propertyName);
      if (value instanceof Activity) {
        put(propertyName, new NamedActivityWrapper(url+"/"+propertyName, (Activity) value));
      }
    }
  }

  public class NamedActivityWrapper implements Activity {
    String name;
    Activity activity;
    public NamedActivityWrapper(String name, Activity activity) {
      this.name = name;
      this.activity = activity;
    }
    @Override
    public ActivityOutput invoke(ActivityInput input) {
      return activity.invoke(input);
    }
    public String getName() {
      return name;
    }
    @Override
    public String toString() {
      return "["+name+" activity]";
    }
  }
}

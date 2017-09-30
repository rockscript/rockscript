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

import io.rockscript.engine.impl.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/** Special JsonObject used as import object that ensures automatic
 * capturing of the propertyName of Activity values and wraps the
 * activities so that the toString shows the property name. */
public class ImportObject extends JsonObject {

  protected String url;

  public ImportObject(String url) {
    this.url = url;
  }

  @Override
  public ImportObject put(String propertyName, Object value) {
    if (value instanceof Activity
        && ! (value instanceof NamedActivity)) {
      put(propertyName, new NamedActivity(url, propertyName, (Activity) value));
    } else {
      super.put(propertyName, value);
    }
    return this;
  }

  public ImportObject put(String propertyName, Function<ActivityInput, ActivityOutput> activityOutput, final String... argNames) {
    Activity activity = new Activity() {
      @Override
      public List<String> getArgNames() {
        return argNames!=null ? Arrays.asList(argNames) : null;
      }
      @Override
      public ActivityOutput invoke(ActivityInput activityInput) {
        return activityOutput.apply(activityInput);
      }
    };
    put(propertyName, activity);
    return this;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return url;
  }
}

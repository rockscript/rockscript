/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
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

import java.util.List;

public class NamedActivity implements Activity {

  String serviceName;
  String activityName;
  Activity activity;

  public NamedActivity(String serviceName, String activityName, Activity activity) {
    this.serviceName = serviceName;
    this.activityName = activityName;
    this.activity = activity;
  }

  @Override
  public List<String> getArgNames() {
    return activity.getArgNames();
  }

  @Override
  public ActivityOutput invoke(ActivityInput input) {
    return activity.invoke(input);
  }

  public String getActivityName() {
    return activityName;
  }

  public String getServiceName() {
    return serviceName;
  }

  @Override
  public String toString() {
    return serviceName+"/"+activityName;
  }
}

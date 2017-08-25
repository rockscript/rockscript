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

package io.rockscript.engine;

import io.rockscript.activity.*;
import io.rockscript.service.Configuration;

public class SystemImportActivity implements Activity {

  Configuration configuration;

  public SystemImportActivity(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public ActivityOutput invoke(ActivityInput input) {
    String url = (String) input.getArgs().get(0);
    JsonObject importedObject = configuration.getImportResolver().get(url);
    return ActivityOutput.endFunction(importedObject);
  }

  @Override
  public String toString() {
    return "[system.import activity]";
  }
}

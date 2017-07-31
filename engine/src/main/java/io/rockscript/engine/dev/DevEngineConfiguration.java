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
package io.rockscript.engine.dev;

import io.rockscript.DevEngine;
import io.rockscript.Engine;
import io.rockscript.engine.ImportResolver;
import io.rockscript.engine.test.TestEngineConfiguration;

public class DevEngineConfiguration extends TestEngineConfiguration {

  public DevEngineConfiguration() {
  }

  @Override
  protected ImportResolver createImportResolver() {
    return new DevImportResolver(this);
  }

  @Override
  public Engine build() {
    return new DevEngine(this);
  }
}

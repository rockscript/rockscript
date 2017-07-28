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
package io.rockscript;

import io.rockscript.action.ActionOutput;
import io.rockscript.action.http.HttpImportProvider;
import io.rockscript.engine.*;

public class DevServer extends Server {

  public DevServer() {
    super(new DevServerConfiguration(new DevEngine()));
  }

  public static class DevServerConfiguration extends ServerConfiguration {
    public DevServerConfiguration(DevEngine devEngine) {
      context(Engine.class, devEngine);
      context(EngineImpl.class, devEngine);
    }
  }

  public static void main(String[] args) {
    DevServer server = new DevServer();
    server.startup();
  }

}

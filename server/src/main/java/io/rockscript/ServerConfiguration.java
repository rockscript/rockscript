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

import io.rockscript.engine.EngineConfiguration;
import io.rockscript.netty.router.AsyncHttpServerConfiguration;
import io.rockscript.netty.router.Context;

public class ServerConfiguration {

  protected AsyncHttpServerConfiguration asyncHttpServerConfiguration = new AsyncHttpServerConfiguration();
  protected EngineConfiguration engineConfiguration;

  public ServerConfiguration(EngineConfiguration engineConfiguration) {
    this.engineConfiguration = engineConfiguration;
  }

  public ServerConfiguration port(int port) {
    asyncHttpServerConfiguration.port(port);
    return this;
  }

  public Server build() {
    return new Server(this);
  }

  AsyncHttpServerConfiguration getAsyncHttpServerConfiguration() {
    return asyncHttpServerConfiguration;
  }

  public EngineConfiguration getEngineConfiguration() {
    return engineConfiguration;
  }
}

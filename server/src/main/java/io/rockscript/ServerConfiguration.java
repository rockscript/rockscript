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

import io.rockscript.netty.router.AsyncHttpServerConfiguration;
import io.rockscript.service.Configuration;

public class ServerConfiguration {

  protected AsyncHttpServerConfiguration asyncHttpServerConfiguration = new AsyncHttpServerConfiguration();
  protected Configuration serviceConfiguration;

  public ServerConfiguration(DevConfiguration serviceConfiguration) {
    this.serviceConfiguration = serviceConfiguration;
  }

  public ServerConfiguration port(int port) {
    asyncHttpServerConfiguration.port(port);
    return this;
  }

  public Server build() {
    ScriptException.throwIfNull(serviceConfiguration, "serviceConfiguration must be configured.  Use .serviceConfiguration(...) before building the server.");
    return new Server(this);
  }

  AsyncHttpServerConfiguration getAsyncHttpServerConfiguration() {
    return asyncHttpServerConfiguration;
  }

  public Configuration getServiceConfiguration() {
    return serviceConfiguration;
  }

  public ServerConfiguration serviceConfiguration(Configuration serviceConfiguration) {
    this.serviceConfiguration = serviceConfiguration;
    return this;
  }
}

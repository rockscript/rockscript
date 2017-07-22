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

package io.rockscript.netty.router;

import com.google.inject.AbstractModule;


/** Guice IoC module for testing */
public class ServerModule extends AbstractModule {

  ServerConfiguration serverConfiguration = null;

  public ServerModule() {
    this(new ServerConfiguration()
        .defaultNotFoundHandler());
  }

  public ServerModule(ServerConfiguration serverConfiguration) {
    this.serverConfiguration = serverConfiguration;
  }

  public ServerModule requestHandlerClass(Class<? extends RequestHandler> requestHandlerClass) {
    serverConfiguration.scan(requestHandlerClass);
    return this;
  }

  @Override
  protected void configure() {
    bind(ServerConfiguration.class).toInstance(serverConfiguration);
    bind(Server.class).toProvider(new ServerProvider());
  }
}

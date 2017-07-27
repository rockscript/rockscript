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
package io.rockscript.handlers;

import com.google.inject.Inject;
import io.rockscript.Engine;
import io.rockscript.netty.router.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Post("/deploy")
public class DeployScriptHandler implements RequestHandler {

  static Logger log = LoggerFactory.getLogger(DeployScriptHandler.class);

  @Inject
  Engine engine;

  @Override
  public void handle(Request request, Response response) {
    String script = request.getBodyStringUtf8();
    String scriptId = engine.deployScript(script);

    response.statusOk();
    response.bodyString(scriptId);
    response.send();
  }
}

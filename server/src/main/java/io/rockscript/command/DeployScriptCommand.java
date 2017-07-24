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
package io.rockscript.command;

import com.google.inject.Inject;
import io.rockscript.Engine;
import io.rockscript.netty.router.Request;
import io.rockscript.netty.router.Response;

public class DeployScriptCommand implements Command {

  @Inject
  Engine engine;

  String script;

  public DeployScriptCommand script(String script) {
    this.script = script;
    return this;
  }

  public static class ResponseJson {
    public String scriptId;
    public ResponseJson scriptId(String scriptId) {
      this.scriptId = scriptId;
      return this;
    }
  }

  @Override
  public void execute(Request request, Response response) {
    String scriptId = engine.deployScript(script);

    response.bodyJson(new ResponseJson()
      .scriptId(scriptId));
    response.statusOk();
    response.send();
  }
}

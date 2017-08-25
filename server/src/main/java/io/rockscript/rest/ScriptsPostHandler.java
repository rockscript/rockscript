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
package io.rockscript.rest;

import io.rockscript.ScriptService;
import io.rockscript.netty.router.*;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;

@Post("/scripts")
public class ScriptsPostHandler implements RequestHandler {

  @Override
  public void handle(Request request, Response response, Context context) {
    String script = request.getBodyStringUtf8();

    String scriptId = context
      .get(ScriptService.class)
      .newDeployScriptCommand()
        .text(script)
        .execute()
      .getId();

    response.statusOk();
    response.bodyJson(hashMap(
      entry("id", scriptId)
    ));
    response.send();
  }
}

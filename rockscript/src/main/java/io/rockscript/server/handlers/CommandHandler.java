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
package io.rockscript.server.handlers;

import io.rockscript.engine.Command;
import io.rockscript.netty.router.*;
import io.rockscript.engine.CommandImpl;
import io.rockscript.engine.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Post("/command")
public class CommandHandler implements RequestHandler {

  static Logger log = LoggerFactory.getLogger(CommandHandler.class);

  @Override
  public void handle(Request request, Response response, Context context) {
    CommandImpl<?> command = (CommandImpl<?>) request.getBodyJson(Command.class);
    Configuration configuration = context.get(Configuration.class);
    command.setConfiguration(configuration);

    Object responseBodyJson = command.execute();

    response.bodyJson(responseBodyJson);
    response.statusOk();
    response.send();
  }
}

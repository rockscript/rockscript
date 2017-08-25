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

import io.rockscript.ScriptService;
import io.rockscript.engine.Event;
import io.rockscript.netty.router.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Get("/events")
public class EventsHandler implements RequestHandler {

  static Logger log = LoggerFactory.getLogger(EventsHandler.class);

  @Override
  public void handle(Request request, Response response, Context context) {
    List<Event> events = context
      .get(ScriptService.class)
      .getConfiguration()
      .getEventStore()
      .getEvents();

    response.bodyJson(events);
    response.statusOk();
    response.send();
  }
}

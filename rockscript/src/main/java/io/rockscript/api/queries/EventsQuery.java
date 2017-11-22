/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
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
package io.rockscript.api.queries;

import io.rockscript.http.servlet.Get;
import io.rockscript.http.servlet.ServerRequest;
import io.rockscript.http.servlet.ServerResponse;
import io.rockscript.http.servlet.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Query to fetch events */
@Get("/events")
public class EventsQuery implements RequestHandler {

  static Logger log = LoggerFactory.getLogger(EventsQuery.class);

  @Override
  public void handle(ServerRequest request, ServerResponse response) {
    throw new UnsupportedOperationException("TODO");
  }

  //  @Override
//  public void handle(AsyncHttpRequest request, AsyncHttpResponse response, Context context) {
//    Engine engine = context.get(Engine.class);
//    String scriptExecutionId = request.getQueryParameter("scriptExecutionId");
//    try {
//      List<? extends Event> events = null;
//      if (scriptExecutionId!=null) {
//        events = engine
//          .getEventStore()
//          .findEventsByScriptExecutionId(scriptExecutionId);
//      } else {
//        events = engine
//          .getEventStore()
//          .getEvents();
//      }
//      response.bodyJson(events);
//      response.status(200);
//      response.send();
//
//    } catch (Exception e) {
//      log.debug("Exception while querying events: " + e.getMessage(), e);
//      response.bodyJson(hashMap(entry("message", "Error: " + e.getMessage())));
//      response.status(500);
//      response.send();
//    }
//  }
}

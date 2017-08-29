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
package io.rockscript.engine;

import io.rockscript.service.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLogger implements EventListener {

  static final Logger eventLog = LoggerFactory.getLogger(EventLogger.class.getName());

  EventListener next;

  public EventLogger(Configuration configuration, EventListener next) {
    this.next = next;
  }

  @Override
  public void handle(Event event) {
    String jsonString = eventJsonToJsonString(event);
    eventLog.debug(jsonString);
    next.handle(event);
  }

  public String eventJsonToJsonString(Event event) {
    return event!=null ? event.toString() : "null";
  }

}

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

import io.rockscript.engine.impl.ExecutionEvent;

import java.util.List;

/** Query to fetch events */
public class EventsQuery extends CommandImpl<EventsResponse> {

  protected String scriptExecutionId;

  public EventsQuery() {
  }

  public EventsQuery(Configuration configuration) {
    super(configuration);
  }

  public EventsQuery scriptExecutionId(String scriptExecutionId) {
    this.scriptExecutionId = scriptExecutionId;
    return this;
  }

  @Override
  protected EventsResponse execute(Configuration configuration) {
    if (scriptExecutionId!=null) {
      List<ExecutionEvent> events = configuration
        .getEventStore()
        .findEventsByScriptExecutionId(scriptExecutionId);
      return new EventsResponse(events);
    }
    return new EventsResponse("Only event queries with script execution id filter are supported at the moment");
  }
}

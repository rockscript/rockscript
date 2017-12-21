/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.rockscript.api.queries;

import io.rockscript.Engine;
import io.rockscript.api.Query;
import io.rockscript.api.events.ExecutionEvent;
import io.rockscript.http.servlet.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/** Query to fetch events for a given scriptExecutionId */
public class EventsQuery implements Query<List<ExecutionEvent>> {

  static Logger log = LoggerFactory.getLogger(EventsQuery.class);

  String scriptExecutionId;
  Integer minIndex;

  @Override
  public String getName() {
    return "events";
  }

  @Override
  public List<ExecutionEvent> execute(Engine engine) {
    BadRequestException.throwIfNull(scriptExecutionId, "scriptExecutionId is a required parameter");

    List<ExecutionEvent> events = engine
      .getScriptExecutionStore()
      .findEventsByScriptExecutionId(scriptExecutionId);

    if (minIndex!=null && minIndex>0) {
      if (minIndex<events.size()) {
        events = events.subList(minIndex, events.size());
      } else {
        events = Collections.EMPTY_LIST;
      }
    }

    return events;
  }
}

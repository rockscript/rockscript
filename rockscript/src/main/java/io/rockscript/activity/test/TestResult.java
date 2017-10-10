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
package io.rockscript.activity.test;

import io.rockscript.engine.impl.ErrorMessage;
import io.rockscript.engine.impl.Event;
import io.rockscript.engine.impl.ScriptExecutionErrorEvent;

import java.util.ArrayList;
import java.util.List;

public class TestResult {

  List<Event> events = new ArrayList<>();
  List<ErrorMessage> errors;

  public void addError(ErrorMessage error) {
    if (errors==null) {
      errors = new ArrayList<>();
    }
    this.errors.add(error);
  }

  public void addEvent(Event event) {
    events.add(event);
    if (event instanceof ScriptExecutionErrorEvent) {
      ScriptExecutionErrorEvent errorEvent = (ScriptExecutionErrorEvent) event;
      addError(new ErrorMessage(errorEvent.getError(), errorEvent.getScriptId(), errorEvent.getLine()));
    }
  }

  public List<Event> getEvents() {
    return events;
  }

  public List<ErrorMessage> getErrors() {
    return errors;
  }
}

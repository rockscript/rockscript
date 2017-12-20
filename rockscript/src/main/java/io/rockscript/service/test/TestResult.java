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
package io.rockscript.service.test;

import io.rockscript.api.events.Event;
import io.rockscript.api.events.ScriptExecutionErrorEvent;

import java.util.ArrayList;
import java.util.List;

public class TestResult {

  String testName;
  List<Event> events = new ArrayList<>();
  List<TestError> errors;
  TestScriptExecution scriptExecution;

  /** for gson serialization */
  protected TestResult() {
  }

  public TestResult(String testName) {
    this.testName = testName;
  }

  public void addError(TestError error) {
    if (errors==null) {
      errors = new ArrayList<>();
    }
    this.errors.add(error);
  }

  public void addEvent(Event event) {
    events.add(event);
    if (event instanceof ScriptExecutionErrorEvent) {
      ScriptExecutionErrorEvent errorEvent = (ScriptExecutionErrorEvent) event;
      addError(new TestError(errorEvent.getError(), errorEvent.getScriptVersionId(), errorEvent.getLine()));
    }
  }

  public List<Event> getEvents() {
    return events;
  }

  public List<TestError> getErrors() {
    return errors;
  }

  public String getTestName() {
    return testName;
  }

  public boolean hasError() {
    return errors!=null && !errors.isEmpty();
  }

  public TestScriptExecution getScriptExecution() {
    return scriptExecution;
  }

  public void setScriptExecution(TestScriptExecution scriptExecution) {
    this.scriptExecution = scriptExecution;
  }
}

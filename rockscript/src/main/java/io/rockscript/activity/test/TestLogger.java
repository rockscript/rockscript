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

import io.rockscript.engine.impl.Event;
import io.rockscript.engine.impl.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogger implements EventListener {
  static final Logger log = LoggerFactory.getLogger(TestLogger.class.getName());

  TestResult testResult;
  EventListener next;
  public TestLogger(TestResult testResult, EventListener next) {
    this.testResult = testResult;
    this.next = next;
  }
  @Override
  public void handle(Event event) {
    testResult.addEvent(event);
    log.debug(event.toString());
    next.handle(event);
  }
}

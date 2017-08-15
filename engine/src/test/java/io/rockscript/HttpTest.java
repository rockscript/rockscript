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
package io.rockscript;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTest {

  protected static Logger log = LoggerFactory.getLogger(HttpTest.class);

  ScriptService scriptService = createTestEngine();
  List<Object> synchronousCapturedData = new ArrayList<>();
  List<String> waitingAsyncFunctionInvocationIds = new ArrayList<>();

  public ScriptService createTestEngine() {
    return new TestScriptService();
  }

  @Test
  public void testAsyncExecution() {
    String scriptId = scriptService
      .deployScript(
        "var http = system.import('rockscript.io/http'); \n" +
        "var requestbin = system.import('rockscript.io/requestbin'); \n" +
        "var rates = http.get({url:'http://api.fixer.io/latest'}); \n" +
        "requestbin.postToBin({ message: '1 EUR is ... USD' }); ")
      .getId();

    log.debug("Starting script...");
    String scriptExecutionId = scriptService
      .startScriptExecution(scriptId)
      .getId();
  }

}

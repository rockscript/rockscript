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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestTest {

  protected static Logger log = LoggerFactory.getLogger(TestTest.class);

  ScriptService scriptService = new TestConfiguration().build();

//  @Test
//  public void testTest() {
//    scriptService
//      .deployScript(
//        "var http = system.import('rockscript.io/http'); \n" +
//        "http.get({url: 'theurl'}); ",
//        "the-get-script.rs");
//
//    scriptService
//      .deployScript(
//        "var test = system.import('rockscript.io/test'); \n" +
//        "test.start({scriptName: 'theget'}); ",
//        "the-test.rst");
//
//    ScriptTestResults scriptTestResult = scriptService.runTests("the-test.rst");
//  }

}

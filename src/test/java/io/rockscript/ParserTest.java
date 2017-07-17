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
package io.rockscript;

import java.util.List;

import io.rockscript.engine.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;

public class ParserTest {

  protected static Logger log = LoggerFactory.getLogger(ParserTest.class);

  @Test
  public void testAsyncExecution() {
    Script script = Parse.parse(
        "var otherService = system.import('example.com/hello'); \n" +
        "var message = 5; \n" +
        "helloService.aSyncFunction(message); \n"+
        "helloService.anAsyncFunction(); \n" +
        "helloService.aSyncFunction('hello');");

    assertNotNull(script);
    List<SourceElement> sourceElements = script.getSourceElements();
    VariableDeclaration variableDeclaration = (VariableDeclaration) sourceElements.get(0);
    assertNotNull(variableDeclaration);

    // ...
  }

}

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
 *
 */
package io.rockscript;

import java.util.ArrayList;
import java.util.List;

import io.rockscript.action.*;
import io.rockscript.engine.*;
import io.rockscript.test.*;
import io.rockscript.test.CrashTestEngine.CrashEventListener;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrashTest {

  static Logger log = LoggerFactory.getLogger(CrashTest.class);

  List<Object> synchronousCapturedData = new ArrayList<>();
  List<String> waitingAsyncFunctionInvocationIds = new ArrayList<>();

  public CrashTestEngine createCrashTestEngine() {
    CrashTestEngine engine = new CrashTestEngine();
    addHelloService(engine);
    return engine;
  }

  public TestEngine createNormalTestEngine() {
    TestEngine engine = new TestEngine();
    addHelloService(engine);
    return engine;
  }

  private void addHelloService(TestEngine engine) {
    engine.getEngineConfiguration()
      .getImportResolver()
      .add("example.com/hello", new ImportJsonObject()
        .put("aSyncFunction", input -> {
          synchronousCapturedData.add("Execution was here");
          synchronousCapturedData.add(input.getArgs().get(0));
          return ActionOutput.endFunction();})
        .put("anAsyncFunction", input -> {
          waitingAsyncFunctionInvocationIds.add(input.getExecutionId());
          return ActionOutput.waitForFunctionToCompleteAsync();}));
  }

  @Test
  public void testCrashRecovery() {
    String scriptText =
      "var helloService = system.import('example.com/hello'); \n" +
      "var message = 5; \n" +
      "helloService.aSyncFunction(message); \n"+
      "helloService.anAsyncFunction(); \n" +
      "helloService.aSyncFunction('hello');";

    ScriptExecution expectedScriptExecutionState = createExpectedScriptExecutionState(scriptText);

    int eventsWithoutCrash = 1;
    boolean crashOccurred = false;
    CrashTestEngine engine = createCrashTestEngine();
    CrashEventListener eventListener = (CrashEventListener) engine.getEngineConfiguration().getEventListener();

    String scriptId = engine
      .deployScript(scriptText)
      .getId();
    do {
      try  {
        crashOccurred = false;

        eventListener.throwAfterEventCount(eventsWithoutCrash);

        log.debug("\n\n----- Starting script execution and throwing after "+eventsWithoutCrash+" events ------");
        ScriptExecution scriptExecution = engine
          .startScriptExecution(scriptId);

      } catch (RuntimeException e) {
        log.debug("----- Recovering script execution and throwing after "+eventsWithoutCrash+" events ------");
        crashOccurred = true;
        eventsWithoutCrash++;

        eventListener.stopThrowing();
        List<ScriptExecution> recoverCrashedScriptExecutions = engine.recoverCrashedScriptExecutions();
        ScriptExecution recoveredScriptExecution = recoverCrashedScriptExecutions.get(0);

        // We don't want to compare the id's
        recoveredScriptExecution.setId(null);
        expectedScriptExecutionState.setId(null);

        new ScriptExecutionComparator()
          .assertEquals(expectedScriptExecutionState, recoveredScriptExecution);
      }

    } while (crashOccurred);
  }

  private ScriptExecution createExpectedScriptExecutionState(String scriptText) {
    TestEngine engine = createNormalTestEngine();
    String scriptId = engine
      .deployScript(scriptText)
      .getId();
    return engine.startScriptExecution(scriptId);
  }
}

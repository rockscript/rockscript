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

import io.rockscript.activity.ActivityOutput;
import io.rockscript.activity.ImportJsonObject;
import io.rockscript.engine.ScriptExecution;
import io.rockscript.test.CrashTestScriptService;
import io.rockscript.test.CrashTestScriptService.CrashEventListener;
import io.rockscript.test.ScriptExecutionComparator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CrashTest {

  static Logger log = LoggerFactory.getLogger(CrashTest.class);

  List<Object> synchronousCapturedData = new ArrayList<>();
  List<String> waitingAsyncFunctionInvocationIds = new ArrayList<>();

  public CrashTestScriptService createCrashTestEngine() {
    CrashTestScriptService engine = new CrashTestScriptService();
    addHelloService(engine);
    return engine;
  }

  public TestScriptService createNormalTestEngine() {
    TestScriptService engine = new TestScriptService();
    addHelloService(engine);
    return engine;
  }

  private void addHelloService(TestScriptService engine) {
    engine.getEngineConfiguration()
      .getImportResolver()
      .add("example.com/hello", new ImportJsonObject()
        .put("aSyncFunction", input -> {
          synchronousCapturedData.add("Execution was here");
          synchronousCapturedData.add(input.getArgs().get(0));
          return ActivityOutput.endFunction();})
        .put("anAsyncFunction", input -> {
          waitingAsyncFunctionInvocationIds.add(input.getExecutionId());
          return ActivityOutput.waitForFunctionToCompleteAsync();}));
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
    CrashTestScriptService scriptService = createCrashTestEngine();
    CrashEventListener eventListener = (CrashEventListener) scriptService
        .getEngineConfiguration()
        .getEventListener();

    String scriptId = scriptService
      .newDeployScriptCommand()
        .text(scriptText)
        .execute()
      .getId();
    do {
      try  {
        crashOccurred = false;

        eventListener.throwAfterEventCount(eventsWithoutCrash);

        log.debug("\n\n----- Starting script execution and throwing after "+eventsWithoutCrash+" events ------");
        ScriptExecution scriptExecution = scriptService
          .startScriptExecution(scriptId);

      } catch (RuntimeException e) {
        log.debug("----- Recovering script execution and throwing after "+eventsWithoutCrash+" events ------");
        crashOccurred = true;
        eventsWithoutCrash++;

        eventListener.stopThrowing();
        List<ScriptExecution> recoverCrashedScriptExecutions = scriptService.recoverCrashedScriptExecutions();
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
    TestScriptService scriptService = createNormalTestEngine();
    String scriptId = scriptService
      .newDeployScriptCommand()
        .text(scriptText)
        .execute()
      .getId();
    return scriptService.startScriptExecution(scriptId);
  }
}

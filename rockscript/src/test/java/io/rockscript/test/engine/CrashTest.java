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
package io.rockscript.test.engine;

import io.rockscript.Engine;
import io.rockscript.TestEngine;
import io.rockscript.activity.ActivityOutput;
import io.rockscript.api.commands.RecoverCrashedScriptExecutionsCommand;
import io.rockscript.api.commands.RecoverCrashedScriptExecutionsResponse;
import io.rockscript.api.commands.SaveScriptVersionCommand;
import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.api.model.ScriptExecution;
import io.rockscript.engine.impl.Event;
import io.rockscript.engine.impl.EventListener;
import io.rockscript.test.ScriptExecutionComparator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CrashTest extends AbstractEngineTest {

  static Logger log = LoggerFactory.getLogger(CrashTest.class);

  List<Object> synchronousCapturedData = new ArrayList<>();
  List<String> waitingAsyncFunctionInvocationIds = new ArrayList<>();

  @Override
  protected Engine initializeEngine() {
    TestEngine engine = new TestEngine();
    addHelloService(engine);
    engine.start();
    return engine;
  }

  protected CrashEngine createCrashEngine() {
    CrashEngine crashEngine = new CrashEngine();
    addHelloService(crashEngine);
    crashEngine.start();
    return crashEngine;
  }

  public static class CrashEngine extends TestEngine {
    public CrashEngine() {
      eventListener = new CrashEventListener(this.eventListener);
    }
  }

  public static class CrashEventListener implements EventListener {
    boolean throwing = false;
    int eventsWithoutCrash;
    int eventCount;
    EventListener target;

    public CrashEventListener(EventListener target) {
      this.eventCount = 0;
      this.target = target;
    }

    public void throwAfterEventCount(int eventsWithoutCrash) {
      this.throwing = true;
      this.eventsWithoutCrash = eventsWithoutCrash;
      this.eventCount = 0;
    }

    public void stopThrowing() {
      this.throwing = false;
    }

    @Override
    public void handle(Event event) {
      if (throwing) {
        if (eventCount>=eventsWithoutCrash) {
          throw new CrashException("Exception after the "+eventCount+"th event");
        }
        eventCount++;
      }
      target.handle(event);
    }
  }

  public static class CrashException extends RuntimeException {
    public CrashException(String message) {
      super(message);
    }
  }

  private void addHelloService(Engine engine) {
    engine.getImportResolver().createImport("example.com/hello")
      .put("aSyncFunction", input -> {
        synchronousCapturedData.add("Execution was here");
        synchronousCapturedData.add(input.getArgs().get(0));
        return ActivityOutput.endActivity();})
      .put("anAsyncFunction", input -> {
        waitingAsyncFunctionInvocationIds.add(input.getExecutionId());
        return ActivityOutput.waitForEndActivityCallback();});
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

    CrashEngine crashEngine = createCrashEngine();
    CrashEventListener eventListener = (CrashEventListener) crashEngine.getEventListener();

    String scriptId = new SaveScriptVersionCommand()
        .scriptText(scriptText)
        .activate()
        .execute(crashEngine)
        .getId();

    do {
      try  {
        crashOccurred = false;

        eventListener.throwAfterEventCount(eventsWithoutCrash);

        log.debug("\n\n----- Starting script execution and throwing after "+eventsWithoutCrash+" events ------");
        new StartScriptExecutionCommand()
          .scriptVersionId(scriptId)
          .execute(crashEngine);

      } catch (CrashException e) {
        log.debug("----- Recovering script execution and throwing after "+eventsWithoutCrash+" events ------");
        crashOccurred = true;
        eventsWithoutCrash++;

        eventListener.stopThrowing();
        RecoverCrashedScriptExecutionsResponse recoverCrashedScriptExecutionsResponse = new RecoverCrashedScriptExecutionsCommand()
          .execute(crashEngine);
        List<ScriptExecution> recoverCrashedScriptExecutions = recoverCrashedScriptExecutionsResponse.getScriptExecutions();
        ScriptExecution recoveredScriptExecution = (ScriptExecution) recoverCrashedScriptExecutions.get(0);

        new ScriptExecutionComparator()
          .ignoreField(ScriptExecution.class, "id")
          .ignoreField(ScriptExecution.class, "start")
          .ignoreField(ScriptExecution.class, "end")
          .assertEquals(expectedScriptExecutionState, recoveredScriptExecution);
      }

    } while (crashOccurred);
  }

  private ScriptExecution createExpectedScriptExecutionState(String scriptText) {
    String scriptVersionId = new SaveScriptVersionCommand()
        .scriptText(scriptText)
        .activate()
        .execute(engine)
        .getId();
    return new StartScriptExecutionCommand()
        .scriptVersionId(scriptVersionId)
        .execute(engine)
        .getEngineScriptExecution()
        .toScriptExecution();
  }
}

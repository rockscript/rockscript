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
import io.rockscript.engine.*;
import io.rockscript.engine.impl.Event;
import io.rockscript.engine.impl.EventListener;
import io.rockscript.cqrs.CommandExecutorService;
import io.rockscript.cqrs.commands.DeployScriptCommand;
import io.rockscript.cqrs.commands.StartScriptExecutionCommand;
import io.rockscript.cqrs.commands.RecoverCrashedScriptExecutionsCommand;
import io.rockscript.cqrs.commands.RecoverCrashedScriptExecutionsResponse;
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

  public Configuration createCrashTestConfiguration() {
    Configuration configuration = new CrashConfiguration();
    addHelloService(configuration);
    return configuration;
  }

  public CommandExecutorService createNormalTestEngine() {
    TestConfiguration configuration = new TestConfiguration();
    addHelloService(configuration);
    return configuration.build();
  }

  public static class CrashConfiguration extends TestConfiguration {
    public CrashConfiguration() {
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
          throw new RuntimeException("Exception after the "+eventCount+"th event");
        }
        eventCount++;
      }
      target.handle(event);
    }
  }

  private void addHelloService(Configuration configuration) {
    configuration.getImportResolver().createImport("example.com/hello")
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
    Configuration configuration = createCrashTestConfiguration();
    CommandExecutorService commandExecutorService = configuration.build();
    CrashEventListener eventListener = (CrashEventListener) configuration
        .getEventListener();

    String scriptId = commandExecutorService.execute(new DeployScriptCommand()
        .scriptText(scriptText))
      .getId();
    do {
      try  {
        crashOccurred = false;

        eventListener.throwAfterEventCount(eventsWithoutCrash);

        log.debug("\n\n----- Starting script execution and throwing after "+eventsWithoutCrash+" events ------");
        commandExecutorService.execute(new StartScriptExecutionCommand()
          .scriptId(scriptId));

      } catch (RuntimeException e) {
        log.debug("----- Recovering script execution and throwing after "+eventsWithoutCrash+" events ------");
        crashOccurred = true;
        eventsWithoutCrash++;

        eventListener.stopThrowing();
        RecoverCrashedScriptExecutionsResponse recoverCrashedScriptExecutionsResponse = commandExecutorService.execute(new RecoverCrashedScriptExecutionsCommand());
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
    CommandExecutorService commandExecutorService = createNormalTestEngine();
    String scriptId = commandExecutorService.execute(new DeployScriptCommand()
        .scriptText(scriptText))
      .getId();
    return commandExecutorService.execute(new StartScriptExecutionCommand()
        .scriptId(scriptId))
      .getEngineScriptExecution()
      .toScriptExecution();
  }
}

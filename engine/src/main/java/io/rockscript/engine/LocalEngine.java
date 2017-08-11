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
package io.rockscript.engine;

import java.util.*;

import com.sun.istack.internal.NotNull;

/** A simple, fast, self contained lock service that does not
 * coordinate lock acquisition with any other system.
 * This is ideal for single node deployments of the scriptService. */
public class LocalEngine implements Engine {

  EngineConfiguration engineConfiguration;
  Map<String, Lock> locks = Collections.synchronizedMap(new HashMap<>());
  Map<String, List<ActionEndRequest>> actionEndBacklog = Collections.synchronizedMap(new HashMap<>());

  public LocalEngine(EngineConfiguration engineConfiguration) {
    this.engineConfiguration = engineConfiguration;
  }

  @Override
  public ScriptExecution startScriptExecution(String scriptId, Object input) {
    Script script = engineConfiguration
      .getScriptStore()
      .findScriptById(scriptId);

    String scriptExecutionId = engineConfiguration
      .getScriptExecutionIdGenerator()
      .createId();

    ScriptExecution scriptExecution = new ScriptExecution(scriptExecutionId, engineConfiguration, script);
    scriptExecution.setInput(input);

    Lock lock = null;
    Engine engine = engineConfiguration.getEngine();
    try {
      lock = acquireLock(scriptExecutionId);

      ScriptStartedEvent scriptStartedEvent = new ScriptStartedEvent(scriptExecution, input);
      scriptExecution.dispatch(scriptStartedEvent);
      scriptExecution.start();

    } finally {
      if (lock!=null) {
        releaseLock(lock);
      }
    }

    return scriptExecution;
  }

  @Override
  public ScriptExecution endWaitingAction(String scriptExecutionId, String executionId, Object result) {
    ScriptExecution scriptExecution =null;
    Lock lock = acquireLockOrAddEndActionRequestToBacklog(scriptExecutionId, executionId, result);
    if (lock!=null) {
      try {
        scriptExecution = engineConfiguration
          .getEventStore()
          .findScriptExecutionById(scriptExecutionId);
        ArgumentsExpressionExecution execution = (ArgumentsExpressionExecution) scriptExecution.findExecutionRecursive(executionId);
        execution.endAction(result);
      } finally {
        if (lock!=null) {
          releaseLock(lock);
        }
      }
    }
    return scriptExecution;
  }

  private synchronized Lock acquireLockOrAddEndActionRequestToBacklog(String scriptExecutionId, String executionId, Object result) {
    Lock lock = acquireLock(scriptExecutionId);
    if (lock==null) {
      // TODO consider a timer to check that the listening didn't mismatch with releasing the lock
      addToActionEndBacklog(new ActionEndRequest(scriptExecutionId, executionId, result));
    }
    return lock;
  }

  public synchronized Lock acquireLock(@NotNull String scriptExecutionId) {
    Lock lock = locks.get(scriptExecutionId);
    if (lock==null) {
      lock = new Lock(scriptExecutionId);
      locks.put(scriptExecutionId, lock);
      return lock;
    }
    return null;
  }

  public synchronized void releaseLock(Lock lock) {
    String scriptExecutionId = lock.getScriptExecutionId();
    locks.remove(scriptExecutionId);
    endActionsFromBacklog(scriptExecutionId);
  }

  private void endActionsFromBacklog(String scriptExecutionId) {List<ActionEndRequest> actionEndRequests = actionEndBacklog.get(scriptExecutionId);
    if (actionEndRequests!=null && !actionEndRequests.isEmpty()) {
      ActionEndRequest nextActionEndRequest = actionEndRequests.remove(0);
      if (actionEndRequests.isEmpty()) {
        actionEndBacklog.remove(scriptExecutionId);
      }
      engineConfiguration
        .getExecutor()
        .execute(new ActionEndRequestRunnable(nextActionEndRequest, engineConfiguration));
    }
  }

  public synchronized void addToActionEndBacklog(@NotNull ActionEndRequest actionEndRequest) {
    String scriptExecutionId = actionEndRequest.getScriptExecutionId();
    List<ActionEndRequest> scriptExecutionLockListeners = actionEndBacklog.get(scriptExecutionId);
    if (scriptExecutionLockListeners==null) {
      scriptExecutionLockListeners = new ArrayList<>();
      actionEndBacklog.put(scriptExecutionId, scriptExecutionLockListeners);
    }
    scriptExecutionLockListeners.add(actionEndRequest);
  }

  public synchronized List<Lock> getLocks() {
    return new ArrayList<>(locks.values());
  }
}

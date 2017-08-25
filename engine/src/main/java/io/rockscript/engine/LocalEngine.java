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

import io.rockscript.service.Configuration;

import java.util.*;


/** A simple, fast, self contained lock service that does not
 * coordinate lock acquisition with any other system.
 * This is ideal for single node deployments of the scriptService. */
public class LocalEngine implements Engine {

  Configuration configuration;
  Map<String, Lock> locks = Collections.synchronizedMap(new HashMap<>());
  Map<String, List<ActivityEndRequest>> activityEndBacklog = Collections.synchronizedMap(new HashMap<>());

  public LocalEngine(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public ScriptExecution startScriptExecution(String scriptId, Object input) {
    ScriptAst scriptAst = configuration
      .getScriptStore()
      .findScriptAstById(scriptId);

    String scriptExecutionId = configuration
      .getScriptExecutionIdGenerator()
      .createId();

    ScriptExecution scriptExecution = new ScriptExecution(scriptExecutionId, configuration, scriptAst);
    scriptExecution.setInput(input);

    Lock lock = null;
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
  public ScriptExecution endActivity(String scriptExecutionId, String executionId, Object result) {
    ScriptExecution scriptExecution =null;
    Lock lock = acquireLockOrAddEndActivityRequestToBacklog(scriptExecutionId, executionId, result);
    if (lock!=null) {
      try {
        scriptExecution = configuration
          .getEventStore()
          .findScriptExecutionById(scriptExecutionId);
        ArgumentsExpressionExecution execution = (ArgumentsExpressionExecution) scriptExecution.findExecutionRecursive(executionId);
        execution.endActivity(result);
      } finally {
        if (lock!=null) {
          releaseLock(lock);
        }
      }
    }
    return scriptExecution;
  }

  private synchronized Lock acquireLockOrAddEndActivityRequestToBacklog(String scriptExecutionId, String executionId, Object result) {
    Lock lock = acquireLock(scriptExecutionId);
    if (lock==null) {
      // TODO consider a timer to check that the listening didn't mismatch with releasing the lock
      addToActivityEndBacklog(new ActivityEndRequest(scriptExecutionId, executionId, result));
    }
    return lock;
  }

  public synchronized Lock acquireLock(String scriptExecutionId) {
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
    endActivitiesFromBacklog(scriptExecutionId);
  }

  private void endActivitiesFromBacklog(String scriptExecutionId) {List<ActivityEndRequest> activityEndRequests = activityEndBacklog
      .get(scriptExecutionId);
    if (activityEndRequests !=null && !activityEndRequests.isEmpty()) {
      ActivityEndRequest nextActivityEndRequest = activityEndRequests.remove(0);
      if (activityEndRequests.isEmpty()) {
        activityEndBacklog.remove(scriptExecutionId);
      }
      configuration
        .getExecutor()
        .execute(new ActivityEndRequestRunnable(nextActivityEndRequest, configuration));
    }
  }

  public synchronized void addToActivityEndBacklog(ActivityEndRequest activityEndRequest) {
    String scriptExecutionId = activityEndRequest.getScriptExecutionId();
    List<ActivityEndRequest> scriptExecutionLockListeners = activityEndBacklog.get(scriptExecutionId);
    if (scriptExecutionLockListeners==null) {
      scriptExecutionLockListeners = new ArrayList<>();
      activityEndBacklog.put(scriptExecutionId, scriptExecutionLockListeners);
    }
    scriptExecutionLockListeners.add(activityEndRequest);
  }

  public synchronized List<Lock> getLocks() {
    return new ArrayList<>(locks.values());
  }
}

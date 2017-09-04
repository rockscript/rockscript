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
package io.rockscript.engine.impl;

import io.rockscript.engine.EngineException;
import io.rockscript.engine.Configuration;

import java.util.*;


/** A simple, fast, single-node engine. */
public class LocalEngine implements Engine {

  Configuration configuration;
  Map<String, Lock> locks = Collections.synchronizedMap(new HashMap<>());
  Map<String, List<ActivityEndRequest>> activityEndBacklog = Collections.synchronizedMap(new HashMap<>());

  public LocalEngine(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public EngineScriptExecution startScriptExecution(String scriptName, String scriptId, Object input) {
    EngineScript engineScript;
    if (scriptId!=null) {
      engineScript = configuration
          .getScriptStore()
          .findScriptAstById(scriptId);
    } else if (scriptName!=null) {
      engineScript = configuration
          .getScriptStore()
          .findLatestScriptAstByName(scriptName);
    } else {
      throw new EngineException("Either scriptName or scriptId are mandatory");
    }

    EngineException.throwIfNull(engineScript, "Script %s not found", (scriptId!=null?scriptId:scriptName));

    String scriptExecutionId = configuration
      .getScriptExecutionIdGenerator()
      .createId();

    EngineScriptExecution scriptExecution = new EngineScriptExecution(scriptExecutionId, configuration, engineScript);
    scriptExecution.setInput(input);

    Lock lock = null;
    try {
      lock = acquireLock(scriptExecutionId);
      scriptExecution.start(input);
      scriptExecution.doWork();

    } finally {
      releaseLock(lock, scriptExecution);
    }

    return scriptExecution;
  }

  @Override
  public EngineScriptExecution endActivity(ContinuationReference continuationReference, Object result) {
    EngineScriptExecution scriptExecution =null;
    Lock lock = acquireLockOrAddEndActivityRequestToBacklog(continuationReference, result);
    if (lock!=null) {
      try {
        scriptExecution = configuration
          .getEventStore()
          .findScriptExecutionById(continuationReference.getScriptExecutionId());
        endActivity(scriptExecution, continuationReference, result);
      } catch (RuntimeException e) {
        e.printStackTrace();
        throw e;
      } finally {
        releaseLock(lock, scriptExecution);
      }
    }
    return scriptExecution;
  }

  @Override
  public void endActivity(EngineScriptExecution lockedScriptExecution, ContinuationReference continuationReference, Object result) {
    String executionId = continuationReference.getExecutionId();
    ArgumentsExpressionExecution execution = (ArgumentsExpressionExecution) lockedScriptExecution
        .findExecutionRecursive(executionId);
    execution.endActivity(result);
    lockedScriptExecution.doWork();
  }

  private synchronized Lock acquireLockOrAddEndActivityRequestToBacklog(ContinuationReference continuationReference, Object result) {
    Lock lock = acquireLock(continuationReference.getScriptExecutionId());
    if (lock==null) {
      // TODO consider a timer to check that the listening didn't mismatch with releasing the lock
      addToActivityEndBacklog(new ActivityEndRequest(continuationReference, result));
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

  public synchronized void releaseLock(Lock lock, EngineScriptExecution lockedScriptExecution) {
    String scriptExecutionId = lockedScriptExecution.getId();
    ActivityEndRequest nextActivityEndRequest = removeNextActivityEndRequest(scriptExecutionId);
    if (nextActivityEndRequest!=null) {
      configuration
          .getExecutor()
          .execute(new ActivityEndRequestRunnable(nextActivityEndRequest, lock, lockedScriptExecution, this));

    } else {
      locks.remove(scriptExecutionId);
    }
  }

  private ActivityEndRequest removeNextActivityEndRequest(String scriptExecutionId) {
    ActivityEndRequest nextActivityEndRequest = null;
        List<ActivityEndRequest> activityEndRequests = activityEndBacklog
      .get(scriptExecutionId);
    if (activityEndRequests !=null && !activityEndRequests.isEmpty()) {
      nextActivityEndRequest = activityEndRequests.remove(0);
      if (activityEndRequests.isEmpty()) {
        activityEndBacklog.remove(scriptExecutionId);
      }
    }
    return nextActivityEndRequest;
  }

  public synchronized void addToActivityEndBacklog(ActivityEndRequest activityEndRequest) {
    String scriptExecutionId = activityEndRequest.getContinuationReference().getScriptExecutionId();
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

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

import io.rockscript.Engine;
import io.rockscript.engine.EngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/** A simple, fast, single-node scriptRunner. */
public class LocalScriptRunner implements ScriptRunner {

  static Logger log = LoggerFactory.getLogger(LocalScriptRunner.class);

  Engine engine;
  Map<String, Lock> locks = Collections.synchronizedMap(new HashMap<>());
  Map<String, List<ServiceFunctionEndRequest>> endBacklog = Collections.synchronizedMap(new HashMap<>());

  public LocalScriptRunner(Engine engine) {
    this.engine = engine;
  }

  @SuppressWarnings("unchecked")
  @Override
  public EngineScriptExecution startScriptExecution(String scriptVersionId, Object input) {
    if (scriptVersionId==null) {
      throw new EngineException("No scriptVersionId specified");
    }

    ScriptStore scriptStore = engine.getScriptStore();
    EngineScript engineScript = scriptStore.findScriptAstByScriptVersionId(scriptVersionId);

    EngineException.throwIfNull(engineScript, "ScriptVersion %s not found", scriptVersionId);

    String scriptExecutionId = engine
      .getScriptExecutionIdGenerator()
      .createId();

    EngineScriptExecution scriptExecution = new EngineScriptExecution(scriptExecutionId, engine, engineScript);
    scriptExecution.setInput(input);

    Lock lock = acquireLock(scriptExecutionId);
    if (lock!=null) {
      try {
        scriptExecution.start(input);
        scriptExecution.doWork();
        releaseLock(lock, scriptExecution);

      } catch(Throwable e) {
        log.debug("Exception while executing script: "+e.getMessage(), e);;
        Execution execution = getExecution(e, scriptExecution);
        scriptExecution.errorEvent = new ScriptExecutionErrorEvent(execution, e.getMessage());
        scriptExecution.dispatch(scriptExecution.errorEvent);
      }
    }

    return scriptExecution;
  }

  private Execution getExecution(Throwable exception, EngineScriptExecution scriptExecution) {
    Execution execution = exception instanceof EngineException ? ((EngineException)exception).getExecution() : null;
    return execution!=null ? execution : scriptExecution;
  }

  @SuppressWarnings("unchecked")
  @Override
  public EngineScriptExecution endFunction(ContinuationReference continuationReference, Object result) {
    Lock lock = acquireLockOrFunctionEndRequestToBacklog(continuationReference, result);
    EngineScriptExecution scriptExecution = null;
    if (lock!=null) {
      try {
        String scriptExecutionId = continuationReference.getScriptExecutionId();
        scriptExecution = engine
          .getEventStore()
          .findScriptExecutionById(scriptExecutionId);
        if (scriptExecution==null) {
          throw new EngineException("ScriptVersion execution "+scriptExecutionId+" doesn't exist");
        }

        endFunction(scriptExecution, continuationReference, result);
        releaseLock(lock, scriptExecution);
      } catch(Throwable e) {
        log.debug("Exception while executing script: "+e.getMessage(), e);;
        scriptExecution.errorEvent = new ScriptExecutionErrorEvent(scriptExecution, e.getMessage());
        scriptExecution.dispatch(scriptExecution.errorEvent);
      }
    }
    return scriptExecution;
  }

  @Override
  public void endFunction(EngineScriptExecution lockedScriptExecution, ContinuationReference continuationReference, Object result) {
    String executionId = continuationReference.getExecutionId();
    ArgumentsExpressionExecution execution = (ArgumentsExpressionExecution) lockedScriptExecution
        .findExecutionRecursive(executionId);
    EngineException.throwIfNull(execution, "Execution %s not found in script execution %s", executionId, lockedScriptExecution.getId());
    execution.endFunction(result);
    lockedScriptExecution.doWork();
  }

  private synchronized Lock acquireLockOrFunctionEndRequestToBacklog(ContinuationReference continuationReference, Object result) {
    Lock lock = acquireLock(continuationReference.getScriptExecutionId());
    if (lock==null) {
      // TODO consider a timer to check that the listening didn't mismatch with releasing the lock
      addToEndBacklog(new ServiceFunctionEndRequest(continuationReference, result));
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
    EngineException.throwIfNull(lock, "Bug: lock is not supposed to be null");
    String scriptExecutionId = lock.getScriptExecutionId();
    ServiceFunctionEndRequest nextServiceFunctionEndRequest = removeNextFunctionEndRequest(scriptExecutionId);
    if (nextServiceFunctionEndRequest!=null) {
      engine
          .getExecutor()
          .execute(new ServiceFunctionEndRequestRunnable(nextServiceFunctionEndRequest, lock, lockedScriptExecution, this));

    } else {
      locks.remove(scriptExecutionId);
    }
  }

  private ServiceFunctionEndRequest removeNextFunctionEndRequest(String scriptExecutionId) {
    ServiceFunctionEndRequest nextServiceFunctionEndRequest = null;
        List<ServiceFunctionEndRequest> serviceFunctionEndRequests = endBacklog
      .get(scriptExecutionId);
    if (serviceFunctionEndRequests!=null && !serviceFunctionEndRequests.isEmpty()) {
      nextServiceFunctionEndRequest = serviceFunctionEndRequests.remove(0);
      if (serviceFunctionEndRequests.isEmpty()) {
        endBacklog.remove(scriptExecutionId);
      }
    }
    return nextServiceFunctionEndRequest;
  }

  public synchronized void addToEndBacklog(ServiceFunctionEndRequest serviceFunctionEndRequest) {
    String scriptExecutionId = serviceFunctionEndRequest.getContinuationReference().getScriptExecutionId();
    List<ServiceFunctionEndRequest> scriptExecutionLockListeners = endBacklog.get(scriptExecutionId);
    if (scriptExecutionLockListeners==null) {
      scriptExecutionLockListeners = new ArrayList<>();
      endBacklog.put(scriptExecutionId, scriptExecutionLockListeners);
    }
    scriptExecutionLockListeners.add(serviceFunctionEndRequest);
  }

  public synchronized List<Lock> getLocks() {
    return new ArrayList<>(locks.values());
  }
}

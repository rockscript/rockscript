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

import java.time.Instant;
import java.util.*;


/** A simple, fast, single-node scriptRunner. */
public class LocalScriptRunner implements ScriptRunner {

  static Logger log = LoggerFactory.getLogger(LocalScriptRunner.class);

  Engine engine;
  Map<String, Lock> locks = Collections.synchronizedMap(new HashMap<>());
  /** maps script execution ids to listeners for that script execution to be unlocked */
  Map<String, List<UnlockListener>> unlockListeners = Collections.synchronizedMap(new HashMap<>());

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
    Lock lock = acquireLock(continuationReference.getScriptExecutionId());
    EngineScriptExecution lockedScriptExecution = null;
    if (lock!=null) {
      String scriptExecutionId = continuationReference.getScriptExecutionId();
      lockedScriptExecution = engine
        .getEventStore()
        .findScriptExecutionById(scriptExecutionId);
      if (lockedScriptExecution==null) {
        throw new EngineException("ScriptVersion execution "+scriptExecutionId+" doesn't exist");
      }
      endFunctionLocked(lockedScriptExecution, lock, continuationReference, result);
    } else {
      // TODO add a check that the continuation ref actually exists
      // TODO consider a timer to check that the listening didn't mismatch with releasing the lock
      addUnlockListener(continuationReference.getScriptExecutionId(), new ServiceFunctionEndRequest(this, continuationReference, result));
    }
    return lockedScriptExecution;
  }

  public void endFunctionLocked(EngineScriptExecution lockedScriptExecution, Lock lock, ContinuationReference continuationReference, Object result) {
    try {
      String executionId = continuationReference.getExecutionId();
      ArgumentsExpressionExecution execution = (ArgumentsExpressionExecution) lockedScriptExecution
        .findExecutionRecursive(executionId);
      EngineException.throwIfNull(execution, "Execution %s not found in script execution %s", executionId, lockedScriptExecution.getId());
      execution.endFunction(result);
      lockedScriptExecution.doWork();
      releaseLock(lock, lockedScriptExecution);
    } catch(Throwable e) {
      handleScriptExecutionExceptionLocked(lockedScriptExecution, e);
    }
  }

  void handleScriptExecutionExceptionLocked(EngineScriptExecution lockedScriptExecution, Throwable exception) {
    log.debug("Exception while executing script: " + exception.getMessage(), exception);
    lockedScriptExecution.errorEvent = new ScriptExecutionErrorEvent(lockedScriptExecution, exception.getMessage());
    lockedScriptExecution.dispatch(lockedScriptExecution.errorEvent);
  }

  /** Potentially recording the service function error should not be in a locked
   * block because it does not trigger script execution.  It only adds an event
   * and optionally schedules a job. */
  @Override
  public EngineScriptExecution serviceFunctionError(ContinuationReference continuationReference, String error, Instant retryTime) {
    Lock lock = acquireLock(continuationReference.getScriptExecutionId());
    EngineScriptExecution lockedScriptExecution = null;
    if (lock!=null) {
      String scriptExecutionId = continuationReference.getScriptExecutionId();
      lockedScriptExecution = engine
        .getEventStore()
        .findScriptExecutionById(scriptExecutionId);
      if (lockedScriptExecution==null) {
        throw new EngineException("ScriptVersion execution "+scriptExecutionId+" doesn't exist");
      }
      serviceFunctionErrorLocked(lockedScriptExecution, lock, continuationReference, error, retryTime);
    } else {
      // TODO add a check that the continuation ref actually exists
      // TODO consider a timer to check that the listening didn't mismatch with releasing the lock
      addUnlockListener(continuationReference.getScriptExecutionId(), new ServiceFunctionErrorRequest(this, continuationReference, error, retryTime));
    }
    return lockedScriptExecution;
  }

  void serviceFunctionErrorLocked(EngineScriptExecution lockedScriptExecution, Lock lock, ContinuationReference continuationReference, String error, Instant retryTime) {
    try {
      String executionId = continuationReference.getExecutionId();
      ArgumentsExpressionExecution execution = (ArgumentsExpressionExecution) lockedScriptExecution
        .findExecutionRecursive(executionId);
      EngineException.throwIfNull(execution, "Execution %s not found in script execution %s", executionId, lockedScriptExecution.getId());

      execution.handleServiceFunctionError(error, retryTime);

      lockedScriptExecution.doWork();
      releaseLock(lock, lockedScriptExecution);

    } catch(Throwable e) {
      handleScriptExecutionExceptionLocked(lockedScriptExecution, e);
    }
  }

  @Override
  public EngineScriptExecution retryFunction(ContinuationReference continuationReference) {
    Lock lock = acquireLock(continuationReference.getScriptExecutionId());
    EngineScriptExecution lockedScriptExecution = null;
    if (lock!=null) {
      String scriptExecutionId = continuationReference.getScriptExecutionId();
      lockedScriptExecution = engine
        .getEventStore()
        .findScriptExecutionById(scriptExecutionId);
      if (lockedScriptExecution==null) {
        throw new EngineException("ScriptVersion execution "+scriptExecutionId+" doesn't exist");
      }
      retryFunctionLocked(lockedScriptExecution, lock, continuationReference);
    } else {
      // TODO add a check that the continuation ref actually exists
      // TODO consider a timer to check that the listening didn't mismatch with releasing the lock
      addUnlockListener(continuationReference.getScriptExecutionId(), new ServiceFunctionRetryRequest(this, continuationReference));
    }
    return lockedScriptExecution;
  }

  void retryFunctionLocked(EngineScriptExecution lockedScriptExecution, Lock lock, ContinuationReference continuationReference) {
    try {
      String executionId = continuationReference.getExecutionId();
      ArgumentsExpressionExecution execution = (ArgumentsExpressionExecution) lockedScriptExecution
        .findExecutionRecursive(executionId);
      EngineException.throwIfNull(execution, "Execution %s not found in script execution %s", executionId, lockedScriptExecution.getId());

      execution.retry();

      lockedScriptExecution.doWork();
      releaseLock(lock, lockedScriptExecution);

    } catch(Throwable e) {
      handleScriptExecutionExceptionLocked(lockedScriptExecution, e);
    }
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
    UnlockListener nextUnlockListener = removeNextUnlockListener(scriptExecutionId);
    if (nextUnlockListener!=null) {
      nextUnlockListener.releasingLock(engine, lock, lockedScriptExecution);

    } else {
      locks.remove(scriptExecutionId);
    }
  }

  private UnlockListener removeNextUnlockListener(String scriptExecutionId) {
    List<UnlockListener> scriptExecutionUnlockListeners = this.unlockListeners.get(scriptExecutionId);
    if (scriptExecutionUnlockListeners!=null && !this.unlockListeners.isEmpty()) {
      try {
        return scriptExecutionUnlockListeners.remove(0);
      } catch (IndexOutOfBoundsException e) {
        return null;
      }
    }
    return null;
  }

  public synchronized void addUnlockListener(String scriptExecutionId, UnlockListener unlockListener) {
    unlockListeners
      .computeIfAbsent(scriptExecutionId, id->new ArrayList<>())
      .add(unlockListener);
  }

  public synchronized List<Lock> getLocks() {
    return new ArrayList<>(locks.values());
  }
}

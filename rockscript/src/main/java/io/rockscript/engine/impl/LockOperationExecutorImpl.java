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
import io.rockscript.api.events.ScriptExecutionErrorEvent;
import io.rockscript.engine.EngineException;
import io.rockscript.http.servlet.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** A simple, fast, single-node lockService. */
public class LockOperationExecutorImpl implements LockOperationExecutor {

  static Logger log = LoggerFactory.getLogger(LockOperationExecutorImpl.class);

  Engine engine;
  LockService lockService;

  public LockOperationExecutorImpl(Engine engine) {
    this.engine = engine;
    this.lockService = engine.getLockService();
  }

  /** Only returns an EngineScriptExecution when this operation was able to acquire a lock
   * from the first time.  When the lock operation is executed later with the unlocklistener, the
   * return value is null. */
  @Override
  public EngineScriptExecution executeInLock(LockOperation lockOperation) {
    EngineScriptExecution lockedScriptExecution = null;
    String scriptExecutionId = lockOperation.getScriptExecutionId();
    Lock lock = lockService.acquireLock(scriptExecutionId);
    if (lock!=null) {
      lockedScriptExecution = lockOperation.getLockedScriptExecution(engine);
      InternalServerException.throwIfNull(lockedScriptExecution, "Bug: LockOperation's should always return a locked script execution or throw a BadRequestException. scriptExecutionId=%s", scriptExecutionId);
      try {
        lockOperation.execute(engine, lock, lockedScriptExecution);
        lockService.releaseLock(lock, lockedScriptExecution);
      } catch (Exception e) {
        log.debug("Exception while executing script: " + e.getMessage(), e);
        Execution execution = getExecution(e, lockedScriptExecution);
        lockedScriptExecution.dispatch(new ScriptExecutionErrorEvent(execution, e.getMessage()));
      }
    } else {
      lockService.addUnlockListener(scriptExecutionId, lockOperation);
    }
    return lockedScriptExecution;
  }

  private Execution getExecution(Throwable exception, EngineScriptExecution scriptExecution) {
    Execution execution = exception instanceof EngineException ? ((EngineException)exception).getExecution() : null;
    return execution!=null ? execution : scriptExecution;
  }
}

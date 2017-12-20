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
package io.rockscript.engine.impl;

import io.rockscript.Engine;
import io.rockscript.engine.EngineException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class LockServiceImpl implements LockService {

  /** maps scriptExecutionIds to locks */
  Map<String, Lock> locks = Collections.synchronizedMap(new HashMap<>());
  /** maps script execution ids to listeners for that script execution to be unlocked */
  Map<String, List<LockReleaseListener>> unlockListeners = Collections.synchronizedMap(new HashMap<>());

  Engine engine;

  public LockServiceImpl(Engine engine) {
    this.engine = engine;
  }

  @Override
  public synchronized Lock acquireLock(String scriptExecutionId) {
    Lock lock = locks.get(scriptExecutionId);
    if (lock==null) {
      lock = new Lock(scriptExecutionId);
      locks.put(scriptExecutionId, lock);
      return lock;
    }
    return null;
  }

  @Override
  public synchronized void releaseLock(Lock lock, EngineScriptExecution lockedScriptExecution) {
    EngineException.throwIfNull(lock, "Bug: lock is not supposed to be null");
    String scriptExecutionId = lock.getScriptExecutionId();
    LockReleaseListener nextLockReleaseListener = removeNextUnlockListener(scriptExecutionId);
    if (nextLockReleaseListener!=null) {
      nextLockReleaseListener.releasingLock(engine, lock, lockedScriptExecution);
      releaseLock(lock, lockedScriptExecution);

    } else {
      locks.remove(scriptExecutionId);
    }
  }

  private LockReleaseListener removeNextUnlockListener(String scriptExecutionId) {
    List<LockReleaseListener> scriptExecutionLockReleaseListeners = this.unlockListeners.get(scriptExecutionId);
    if (scriptExecutionLockReleaseListeners!=null && !this.unlockListeners.isEmpty()) {
      try {
        return scriptExecutionLockReleaseListeners.remove(0);
      } catch (IndexOutOfBoundsException e) {
        return null;
      }
    }
    return null;
  }

  @Override
  public synchronized void addUnlockListener(String scriptExecutionId, LockReleaseListener lockReleaseListener) {
    unlockListeners
      .computeIfAbsent(scriptExecutionId, id->new ArrayList<>())
      .add(lockReleaseListener);
  }

  public synchronized List<Lock> getLocks() {
    return new ArrayList<>(locks.values());
  }

  public List<Lock> getLocksOlderThan(Instant time) {
    return locks.values().stream()
      .filter(lock->lock.getCreateTime().isBefore(time))
      .collect(Collectors.toList());
  }
}

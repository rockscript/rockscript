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
 */
package io.rockscript.engine.test;

import java.util.*;

import io.rockscript.engine.*;

public class TestLockService implements LockService {

  Map<String, Lock> locks = Collections.synchronizedMap(new HashMap<String, Lock>());

  public TestLockService(EngineConfiguration engineConfiguration) {
  }

  @Override
  public synchronized void newScriptExecution(ScriptExecution scriptExecution, String clientId) {
    locks.put(scriptExecution.getId(), new Lock(scriptExecution, clientId));
  }

  @Override
  public synchronized Lock lockScriptExecution(String scriptExecutionId, String clientId) {
    Lock lock = locks.get(scriptExecutionId);
    if (lock==null) {
      lock = new Lock(scriptExecutionId, clientId);
      locks.put(scriptExecutionId, lock);
      return lock;
    }
    if (clientId.equals(lock.getClientId())) {
      return lock;
    }
    return null;
  }

  @Override
  public synchronized void releaseScriptExecution(ScriptExecution scriptExecution) {
    locks.remove(scriptExecution.getId());
  }

  @Override
  public synchronized List<Lock> getLocks() {
    return new ArrayList<>(locks.values());
  }
}

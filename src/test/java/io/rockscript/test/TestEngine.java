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

package io.rockscript.test;

import io.rockscript.*;
import io.rockscript.engine.EngineImpl;
import io.rockscript.engine.ScriptExecutionIdGenerator;

public class TestEngine extends EngineImpl {

  private static int nextUniqueId = 1;

  public TestEngine() {
    super(new TestServiceLocator(), nextUniqueId());
  }

  public TestEngine(ServiceLocator serviceLocator) {
    super(serviceLocator, nextUniqueId());
  }

  private static String nextUniqueId() {
    return "t"+nextUniqueId++;
  }

  public static class TestServiceLocator extends ServiceLocator {
    public TestServiceLocator() {
      scriptExecutionIdGenerator = new TestIdGenerator();
    }
  }

  public static class TestIdGenerator implements ScriptExecutionIdGenerator {
    int nextId = 1;
    @Override
    public String generateId() {
      return "se"+Integer.toString(nextId++);
    }
  }
}

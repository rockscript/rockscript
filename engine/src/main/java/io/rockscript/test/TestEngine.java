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

import io.rockscript.ServiceLocator;
import io.rockscript.engine.EngineImpl;
import io.rockscript.engine.IdGenerator;

public class TestEngine extends EngineImpl {

  private static int nextTestEngineId = 1;

  public TestEngine() {
    super(new TestServiceLocator(), nextUniqueId());
  }

  public TestEngine(ServiceLocator serviceLocator) {
    super(serviceLocator, nextUniqueId());
  }

  private static String nextUniqueId() {
    return "TestEngine"+nextTestEngineId++;
  }

  public static class TestServiceLocator extends ServiceLocator {
    public TestServiceLocator() {
      scriptIdGenerator = new TestIdGenerator("s");
      scriptExecutionIdGenerator = new TestIdGenerator("e");
    }
  }

  public static class TestIdGenerator implements IdGenerator {
    int nextId = 1;
    String prefix;
    public TestIdGenerator(String prefix) {
      this.prefix = prefix;
    }
    @Override
    public String createId() {
      return prefix+Integer.toString(nextId++);
    }
  }
}

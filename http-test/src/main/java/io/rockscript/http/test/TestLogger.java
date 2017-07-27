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
package io.rockscript.http.test;

import org.junit.rules.TestName;
import org.junit.runner.Description;

public class TestLogger extends TestName {

  @Override
  protected void starting(Description d) {
    super.starting(d);
    logStarting();
  }

  @Override
  protected void succeeded(Description description) {
    super.succeeded(description);
    logEnded();
  }

  @Override
  protected void failed(Throwable e, Description description) {
    super.failed(e, description);
    logEnded();
    e.printStackTrace();
  }

  private void logStarting() {
    System.err.println("=== starting "+getClass().getSimpleName()+"."+getMethodName()+" ====================== \n");
  }

  private void logEnded() {
    System.err.println("====================== "+getClass().getSimpleName()+"."+getMethodName()+" ended === \n");
  }
}

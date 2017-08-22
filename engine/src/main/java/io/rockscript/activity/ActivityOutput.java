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
package io.rockscript.activity;

public class ActivityOutput {

  private final boolean ended;
  private final Object result;

  protected ActivityOutput(boolean ended, Object result) {
    this.ended = ended;
    this.result = result;
  }

  public static ActivityOutput endFunction() {
    return endFunction(null);
  }

  public static ActivityOutput endFunction(Object result) {
    return new ActivityOutput(true, result);
  }

  public static ActivityOutput waitForFunctionToCompleteAsync() {
    return new ActivityOutput(false, null);
  }

  public boolean isEnded() {
    return ended;
  }

  public Object getResult() {
    return result;
  }
}

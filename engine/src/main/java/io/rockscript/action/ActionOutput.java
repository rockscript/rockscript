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
package io.rockscript.action;

public class ActionOutput {

  private final boolean ended;
  private final Object result;

  protected ActionOutput(boolean ended, Object result) {
    this.ended = ended;
    this.result = result;
  }

  public static ActionOutput endFunction() {
    return endFunction(null);
  }

  public static ActionOutput endFunction(Object result) {
    return new ActionOutput(true, result);
  }

  public static ActionOutput waitForFunctionToCompleteAsync() {
    return new ActionOutput(false, null);
  }

  public boolean isEnded() {
    return ended;
  }

  public Object getResult() {
    return result;
  }
}

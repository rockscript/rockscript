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

import io.rockscript.engine.job.RetryPolicy;

public class ActivityOutput {

  private boolean ended;
  private Object result;
  private String error;
  private RetryPolicy retryPolicy;

  public ActivityOutput() {
  }

  protected ActivityOutput(boolean ended, Object result) {
    this.ended = ended;
    this.result = result;
  }

  public ActivityOutput(String error, RetryPolicy retryPolicy) {
    this.error = error;
    this.retryPolicy = retryPolicy;
  }

  public static ActivityOutput error(String error) {
    return error(error, null);
  }

  public static ActivityOutput error(String error, RetryPolicy retryPolicy) {
    return new ActivityOutput(error, retryPolicy);
  }

  public static ActivityOutput waitForFunctionToCompleteAsync() {
    return new ActivityOutput(false, null);
  }

  public static ActivityOutput endFunction() {
    return endFunction(null);
  }

  public static ActivityOutput endFunction(Object result) {
    return new ActivityOutput(true, result);
  }

  public boolean isEnded() {
    return ended;
  }

  public Object getResult() {
    return result;
  }

  public boolean isError() {
    return error!=null;
  }

  public String getError() {
    return error;
  }

  public RetryPolicy getRetryPolicy() {
    return retryPolicy;
  }
}

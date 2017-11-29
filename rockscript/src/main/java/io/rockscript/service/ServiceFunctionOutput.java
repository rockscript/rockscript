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
package io.rockscript.service;

import io.rockscript.engine.job.RetryPolicy;

public class ServiceFunctionOutput {

  private boolean ended;
  private Object result;
  private String error;
  private RetryPolicy retryPolicy;

  public ServiceFunctionOutput() {
  }

  protected ServiceFunctionOutput(boolean ended, Object result) {
    this.ended = ended;
    this.result = result;
  }

  public ServiceFunctionOutput(String error, RetryPolicy retryPolicy) {
    this.error = error;
    this.retryPolicy = retryPolicy;
  }

  public static ServiceFunctionOutput error(String error) {
    return error(error, null);
  }

  public static ServiceFunctionOutput error(String error, RetryPolicy retryPolicy) {
    return new ServiceFunctionOutput(error, retryPolicy);
  }

  public static ServiceFunctionOutput waitForFunctionEndCallback() {
    return new ServiceFunctionOutput(false, null);
  }

  public static ServiceFunctionOutput endFunction() {
    return endFunction(null);
  }

  public static ServiceFunctionOutput endFunction(Object result) {
    return new ServiceFunctionOutput(true, result);
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

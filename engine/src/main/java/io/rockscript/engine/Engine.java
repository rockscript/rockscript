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

package io.rockscript.engine;

/** Performs script execution and ensures that only 1 scriptService
 * is executing a ScriptExecution at any given time.
 * The goal is to also have an in-memory
 * implementation for single-node deployments and a distributed
 * implementations based on Consul. */
public interface Engine {

  ScriptExecution startScriptExecution(String scriptId, Object input);

  ScriptExecution endActivity(String scriptExecutionId, String executionId, Object result);
}

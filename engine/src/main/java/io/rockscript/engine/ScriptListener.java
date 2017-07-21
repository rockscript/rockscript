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

/** To be registered added with {@link Script#addScriptListener(be.tombaeyens.script.parser.ScriptListener)}
 * to a script to receive notifications each time the script is starting. */
public interface ScriptListener {

  /** Called each time a script is starting;
   *  The id of the scriptExecution is already initialized when this is called,
   *  but no statements have yet been started. */
  void starting(ScriptExecution scriptExecution);
}

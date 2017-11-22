/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.rockscript.test;

import io.rockscript.activity.Activity;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.engine.impl.Execution;
import io.rockscript.engine.impl.EngineScript;
import io.rockscript.engine.impl.SystemImportActivity;

import java.util.function.Function;

public class ScriptExecutionComparator extends DeepComparator {

  public ScriptExecutionComparator() {
    ignoreField(EngineScriptExecution.class, "eventListener");
    ignoreField(Execution.class, "element");
    ignoreField(EngineScript.class, "elements");
    ignoreField(EngineScript.class, "engine");
    ignoreField(SystemImportActivity.class, "engine");
    ignoreAnonymousField(Activity.class, "val$functionHandler");
    ignoreAnonymousField(Activity.class, "arg$1");
    ignoreAnonymousField(Function.class, "arg$1");
  }
}

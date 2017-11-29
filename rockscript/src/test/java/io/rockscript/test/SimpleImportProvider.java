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

import io.rockscript.Engine;
import io.rockscript.service.*;
import io.rockscript.api.commands.EndServiceFunctionCommand;
import io.rockscript.api.commands.ScriptExecutionResponse;
import io.rockscript.engine.impl.ContinuationReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleImportProvider extends ImportObject implements ImportProvider {

  protected static Map<String,List<ContinuationReference>> waits;
  
  public static void setUp() {
    waits = new HashMap<>();
  }

  public static ScriptExecutionResponse endWait(String scriptExecutionId, Engine engine) {
    ContinuationReference continuationReference = removeFirstContinuationReference(scriptExecutionId);
    return new EndServiceFunctionCommand()
      .scriptExecutionId(scriptExecutionId)
      .continuationReference(continuationReference)
      .execute(engine);
  }

  public static ContinuationReference removeFirstContinuationReference(String scriptExecutionId) {
    checkWaitsInitialized();
    List<ContinuationReference> continuationReferences = waits.get(scriptExecutionId);
    if (continuationReferences==null) throw new RuntimeException("No continuation references for script execution "+scriptExecutionId);
    return continuationReferences.remove(0);
  }

  public SimpleImportProvider() {
    super("rockscript.io/simple");
    put(new AbstractServiceFunction("wait", new String[]{}) {
      @Override
      public ServiceFunctionOutput invoke(ServiceFunctionInput input) {
        checkWaitsInitialized();
        waits
          .computeIfAbsent(input.getScriptExecutionId(), seid->new ArrayList<>())
          .add(input.getContinuationReference());
        return ServiceFunctionOutput.waitForFunctionEndCallback();
      }
    });
    put(new AbstractServiceFunction("noop", new String[]{}) {
      @Override
      public ServiceFunctionOutput invoke(ServiceFunctionInput input) {
        return ServiceFunctionOutput.endFunction("noop");
      }
    });
  }

  private static void checkWaitsInitialized() {
    if (waits==null) throw new RuntimeException("In the setUp of your test you have to call SimpleImportProvider.setUp();");
  }

  @Override
  public ImportObject getImportObject() {
    return this;
  }
}

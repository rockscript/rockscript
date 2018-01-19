/*
 * Copyright (c) 2018 RockScript.io.
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

import io.rockscript.service.AbstractServiceFunction;
import io.rockscript.service.ImportObject;
import io.rockscript.service.ServiceFunctionInput;
import io.rockscript.service.ServiceFunctionOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TesterImportObject extends ImportObject {

  public static final String CONTEXT_KEY_INVOCATIONS = "invocations";
  public static final String CONTEXT_KEY_RETURN_VALUES = "returnValues";

  public TesterImportObject() {
    super("tester");
    put(new AbstractServiceFunction("invoke", new String[]{}) {
      @Override
      public ServiceFunctionOutput invoke(ServiceFunctionInput input) {
        Map<Object, Object> engineContext = input
          .getEngine()
          .getContext();

        List<ServiceFunctionInput> recordedInputs = (List<ServiceFunctionInput>) engineContext
          .computeIfAbsent(CONTEXT_KEY_INVOCATIONS, k->new ArrayList<>());
        recordedInputs.add(input);

        Object result = input; // the input is returned by default if no return value specified

        List<Object> returnValues = (List<Object>) engineContext.get(CONTEXT_KEY_RETURN_VALUES);
        if (returnValues!=null && !returnValues.isEmpty()) {
          result = returnValues.remove(0);
        }

        return ServiceFunctionOutput.endFunction(result);
      }
    });
  }
}

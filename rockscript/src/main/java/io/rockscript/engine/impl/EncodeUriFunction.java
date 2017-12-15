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
package io.rockscript.engine.impl;

import io.rockscript.service.ServiceFunction;
import io.rockscript.service.ServiceFunctionInput;
import io.rockscript.service.ServiceFunctionOutput;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

public class EncodeUriFunction implements ServiceFunction{

  public static EncodeUriFunction INSTANCE = new EncodeUriFunction();

  @Override
  public String getFunctionName() {
    return "encodeURI";
  }

  @Override
  public String getServiceName() {
    return "system";
  }

  @Override
  public List<String> getArgNames() {
    return Arrays.asList("text");
  }

  @Override
  public ServiceFunctionOutput invoke(ServiceFunctionInput input) {
    try {
      String text = input.getArg(0);
      String encodedText = URLEncoder.encode(text, "UTF-8");
      return ServiceFunctionOutput.endFunction(encodedText);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Bug: please report as a GitHub issue: https://github.com/rockscript/rockscript/issues/new");
    }
  }
}

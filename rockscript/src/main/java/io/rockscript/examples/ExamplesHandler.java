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
package io.rockscript.examples;

import com.google.gson.Gson;
import io.rockscript.Engine;
import io.rockscript.api.AbstractRequestHandler;
import io.rockscript.api.Command;
import io.rockscript.http.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExamplesHandler extends AbstractRequestHandler {

  static Logger log = LoggerFactory.getLogger(ExamplesHandler.class);

  boolean returnError = false;

  public ExamplesHandler(Engine engine) {
    super(GET, "/examples/lucky", engine);
  }

  @Override
  public void handle(ServerRequest request, ServerResponse response) {
    if (returnError) {
      response.statusOk();
      response.bodyString("You're lucky");
      returnError = true;
    } else {
      response.statusInternalServerError();
      response.bodyString("Better luck next time");
      returnError = false;
    }
  }
}

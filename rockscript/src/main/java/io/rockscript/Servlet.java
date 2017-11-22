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
package io.rockscript;

import io.rockscript.api.CommandHandler;
import io.rockscript.engine.PingHandler;
import io.rockscript.http.servlet.RouterServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class Servlet extends RouterServlet {

  static Logger log = LoggerFactory.getLogger(Servlet.class);

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    log.debug(" ____            _     ____            _       _    ");
    log.debug("|  _ \\ ___   ___| | __/ ___|  ___ _ __(_)_ __ | |_  ");
    log.debug("| |_) / _ \\ / __| |/ /\\___ \\ / __| '__| | '_ \\| __| ");
    log.debug("|  _ < (_) | (__|   <  ___) | (__| |  | | |_) | |_  ");
    log.debug("|_| \\_\\___/ \\___|_|\\_\\|____/ \\___|_|  |_| .__/ \\__| ");
    log.debug("                                        |_|         ");

    Engine engine = new DevEngine()
      .initialize();

    requestHandler(new CommandHandler(engine));
    requestHandler(new PingHandler(engine));

    defaultResponseHeader("Access-Control-Allow-Origin", "*");
  }
}

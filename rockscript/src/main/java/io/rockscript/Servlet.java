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

import io.rockscript.engine.impl.MonitoringExecutor;
import io.rockscript.http.servlet.RouterServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class Servlet extends RouterServlet {

  static Logger log = LoggerFactory.getLogger(Servlet.class);

  private Engine engine;

  public Servlet() {
  }

  public Servlet(Engine engine) {
    this.engine = engine;
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    if (engine==null) {
      Map<String,String> configuration = readConfiguration(config);
      engine = createEngine(configuration);
    }

    engine.start();

    log.debug(" ____            _     ____            _       _    ");
    log.debug("|  _ \\ ___   ___| | __/ ___|  ___ _ __(_)_ __ | |_  ");
    log.debug("| |_) / _ \\ / __| |/ /\\___ \\ / __| '__| | '_ \\| __| ");
    log.debug("|  _ < (_) | (__|   <  ___) | (__| |  | | |_) | |_  ");
    log.debug("|_| \\_\\___/ \\___|_|\\_\\|____/ \\___|_|  |_| .__/ \\__| ");
    log.debug("                                        |_|         ");

    setGson(engine.getGson());

    requestHandler(engine.getCommandHandler());
    requestHandler(engine.getQueryHandler());
    requestHandler(engine.getPingHandler());
    requestHandler(engine.getExamplesHandler());
    requestHandler(engine.getFileHandler());

    defaultResponseHeader("Access-Control-Allow-Origin", "*");
  }

  protected Engine createEngine(Map<String,String> configuration) {
    return new Engine(configuration);
  }

  protected Map<String, String> readConfiguration(ServletConfig config) {
    Map<String,String> initParameters = new LinkedHashMap<>();
    Enumeration<String> initParameterNames = config.getInitParameterNames();
    while (initParameterNames.hasMoreElements()) {
      String initParameterName = initParameterNames.nextElement();
      String initParameterValue = config.getInitParameter(initParameterName);
      initParameters.put(initParameterName, initParameterValue);
    }
    return initParameters;
  }
}

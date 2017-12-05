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

import io.rockscript.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Stores all messages relevant for monitoring the RockScript server
 * from an operations point of view. */
public class EngineLogStore {

  static Logger log = LoggerFactory.getLogger(EngineLogStore.class);

  public static final String LEVEL_INFO = "INFO";
  public static final String LEVEL_WARNING = "WARNING";
  public static final String LEVEL_ERROR = "ERROR";

  Engine engine;

  List<EngineLog> logs = new ArrayList<>();

  public EngineLogStore(Engine engine) {
    this.engine = engine;
  }

  public void info(String message) {
    log(LEVEL_INFO, message, null);
  }

  public void info(String message, Throwable exception) {
    log(LEVEL_INFO, message, exception);
  }

  public void warning(String message) {
    log(LEVEL_WARNING, message, null);
  }

  public void warning(String message, Throwable exception) {
    log(LEVEL_WARNING, message, exception);
  }

  public void error(String message) {
    log(LEVEL_ERROR, message, null);
  }

  public void error(String message, Throwable exception) {
    log(LEVEL_ERROR, message, exception);
  }

  public void log(String level, String message) {
    log(level, message, null);
  }

  public void log(String level, String message, Throwable exception) {
    String logMessage = getStackTraceString(message, exception);
    add(new EngineLog(Time.now(), level, logMessage));
  }

  private void add(EngineLog log) {
    EngineLogStore.log.debug("Adding log "+log);
    logs.add(log);
  }

  private String getStackTraceString(String message, Throwable exception) {
    if (message==null) {
      message = "";
    }
    if (exception==null) {
      return message;
    }
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    exception.printStackTrace(pw);
    pw.flush();
    return message+": \n"+sw.toString();
  }
}

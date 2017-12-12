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

import io.rockscript.http.client.HttpClient;
import io.rockscript.test.TestEngine;
import org.apache.commons.cli.*;

import java.util.Map;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;

public abstract class CliCommand {

  static Map<String,Class<? extends CliCommand>> COMMAND_CLASSES = hashMap(
    entry("ping", Ping.class),
    entry("deploy", Deploy.class),
    entry("start", Start.class),
    entry("test", Test.class)
  );

  protected String[] args;
  protected CommandLine commandLine;

  public abstract void execute();
  protected abstract void logCommandUsage();
  protected abstract Options getOptions();

  protected void parse(CommandLine commandLine) {
    this.commandLine = commandLine;
  }

  static CliCommand createCliCommand(String command) {
    try {
      Class<? extends CliCommand> rockClass = COMMAND_CLASSES.get(command);
      if (rockClass==null) {
        Rock.logCommandsOverview(command);
        return null;
      }
      return rockClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public CliCommand parseArgs(String... args) {
    this.args = args;
    Options options = getOptions();
    if (options!=null) {
      CommandLineParser commandLineParser = new DefaultParser();
      try {
        CommandLine commandLine = commandLineParser.parse(options, args);
        parse(commandLine);
      } catch (ParseException e) {
        throw new RuntimeException("Async ClientRequest line args parsing exception: "+e.getMessage(), e);
      }
    }
    return this;
  }

  protected void logCommandUsage(String usage) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(usage, getOptions());
  }

  protected HttpClient createHttp() {
    return new HttpClient(new TestEngine().start().getGson());
  }
}

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

import io.rockscript.http.client.ClientRequest;
import io.rockscript.http.client.ClientResponse;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public abstract class ClientCommand extends CliCommand {

  protected String server = "http://localhost:3652";
  protected boolean quiet = false;

  protected static int MAX_LOG_LENGTH = 120;

  protected Options getOptions() {
    Options options = new Options();
    options.addOption(Option.builder("s")
      .hasArg()
      .desc("The server URL.  Default value is http://localhost:3652")
      .build());
    options.addOption(Option.builder("q")
      .desc("Quiet.  Don't show the HTTP requests to the server.")
      .build());
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
    super.parse(commandLine);
    this.server = commandLine.getOptionValue("s", server);
    this.quiet = commandLine.hasOption("q");
  }

  public String getServer() {
    return this.server;
  }
  public void setServer(String server) {
    this.server = server;
  }
  public ClientCommand server(String server) {
    this.server = server;
    return this;
  }

  protected void log() {
    if (!quiet) Rock.log();
  }

  protected void log(String message) {
    if (!quiet) Rock.log(message);
  }

  protected void log(ClientResponse response) {
    if (!quiet) Rock.log(response.toString("  "));
  }

  protected void log(ClientRequest request) {
    if (!quiet) Rock.log(request.toString("  ", MAX_LOG_LENGTH));
  }
}

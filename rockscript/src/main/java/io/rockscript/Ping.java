/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

public class Ping extends Rock {

  String url = "http://localhost:3652";

  private static Options createCommandLineOptions() {
    Options options = new Options();
    options.addOption("url", "The url of the server.  Default value is http://localhost:3652");
    return options;
  }

  public boolean parse(String[] args) {
    try {
      CommandLine commandLine = parseCommandLine(createCommandLineOptions(), args);
      if (commandLine.hasOption("url")) {
        this.url = commandLine.getOptionValue("url");
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  void execute() throws Exception {
    try {
      Response response = Request.Get(url)
        .execute();
      int statusCode = response.returnResponse().getStatusLine().getStatusCode();
      String body = response.returnContent().asString();
      if (statusCode==200) {
        log("Success. The server returned:");
      } else {
        log("Server did respond, but with an error:");
      }
      log(body);
    } catch (Exception e) {
      log("Could not connect to "+url+" : "+e.getMessage());
    }
  }

  @Override
  void showHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "rock ping [ping options]", createCommandLineOptions());
  }
}

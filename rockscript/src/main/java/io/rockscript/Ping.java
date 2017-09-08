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

import io.rockscript.http.Http;
import io.rockscript.http.HttpRequest;
import io.rockscript.http.HttpResponse;

import static io.rockscript.Rock.log;

public class Ping extends ClientCommand {

  protected String url = "http://localhost:3652";

  @Override
  protected void showCommandUsage() {
    log("rock deploy : Tests the connection with the server");
    log();
    logCommandUsage("rock ping [ping options]");
    log();
    log("Example:");
    log("  rock ping");
    log("Sends a ping request to the server and logs the pong response");
  }

  @Override
  public void execute() throws Exception {
    try {
      String pingUrl = this.url + "/ping";

      log("Pinging server "+url+" ...");

      HttpRequest request = new Http()
        .newGet(pingUrl);

      if (!quiet) log(request.toString("  "));

      HttpResponse response = request.execute();

      if (!quiet) log(response.toString("  "));

      int status = response.getStatus();
      String body = response.getBodyAsString();
      if (status==200) {
        log("Successfully pinged the server");
      } else {
        log("Wrong response status: "+status);
      }
    } catch (Exception e) {
      log("Could not connect to "+url+" : "+e.getMessage());
    }
  }
}

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
import io.rockscript.http.client.HttpClient;

public class Ping extends ClientCommand {

  @Override
  protected void logCommandUsage() {
    log("rock deploy : Tests the connection with the server");
    log();
    logCommandUsage("rock ping [ping options]");
    log();
    log("Example:");
    log("  rock ping");
    log("Sends a ping request to the server and logs the pong response");
  }

  @Override
  public void execute() {
    try {
      String pingUrl = this.server + "/ping";

      log("Pinging server " + server + " ...");

       ClientRequest request = new HttpClient()
        .newGet(pingUrl);

      log(request);

      ClientResponse response = request.execute();

      log(response);

      int status = response.getStatus();
      String body = response.getBody();
      if (status==200) {
        log("Successfully pinged the server");
      } else {
        log("Wrong response status: "+status);
      }
    } catch (Exception e) {
      log("Could not connect to " + server + " : " + e.getMessage());
    }
  }
}

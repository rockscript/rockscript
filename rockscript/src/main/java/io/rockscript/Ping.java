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
import io.rockscript.http.HttpResponse;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class Ping extends Rock {

  protected String url = "http://localhost:3652";

  @Override
  public String getCommandName() {
    return "ping";
  }

  protected Options createOptions() {
    Options options = new Options();
    options.addOption("url", "The url of the server.  Default value is http://localhost:3652");
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
    this.url = commandLine.getOptionValue("url", url);
  }

  @Override
  public void execute() throws Exception {
    try {
      HttpResponse response = new Http()
        .newGet(url + "/ping")
        .execute();

      int status = response.getStatus();
      String body = response.getBodyAsString();
      if (status==200) {
        log("200 OK");
      } else {
        log("Wrong status: "+status);
      }
      log(body);
    } catch (Exception e) {
      log("Could not connect to "+url+" : "+e.getMessage());
    }
  }

  public String getUrl() {
    return this.url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public Ping url(String url) {
    this.url = url;
    return this;
  }
}

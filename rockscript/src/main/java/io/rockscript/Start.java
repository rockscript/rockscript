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

import io.rockscript.engine.StartScriptExecutionCommand;
import io.rockscript.engine.StartScriptExecutionResponse;
import io.rockscript.http.HttpRequest;
import io.rockscript.http.HttpResponse;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Properties;

import static io.rockscript.Rock.log;

public class Start extends ClientCommand {

  protected String scriptName;
  protected String scriptId;
  protected Properties input;

  @Override
  protected void logCommandUsage() {
    log("rock start : Starts a new script execution");
    log();
    logCommandUsage("rock start [start options]");
    log();
    log("Example:");
    log("  rock start -n \"Create approval.rs\"");
    log("Starts a new script execution in the latest version ");
    log("of the script with name ending with 'create-approval.rs'");
  }

  @Override
  protected Options getOptions() {
    Options options = super.getOptions();
    options.addOption(Option.builder("n")
      .desc("Script name. The latest version of the script with the " +
            "given name will be started.  The name has to identify one " +
            "script by matching the last part so you don't have to type " +
            "the full name. It's required to provide either sn or sid.")
      .hasArg()
      .build());
    options.addOption(Option.builder("sid")
      .desc("Script id. This identifies the specific version of a script" +
            "to start. It's required to provide either sn or sid.")
      .hasArg()
      .build());
    options.addOption(Option.builder("p")
      .desc("Input property <propertyName>:<propertyValue>. " +
            "The property will be added to the JSON input object.")
      .numberOfArgs(2)
      .valueSeparator(':')
      .build());
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
    super.parse(commandLine);
    this.scriptName = commandLine.getOptionValue("n");
    this.scriptId = commandLine.getOptionValue("sid");
    this.input = commandLine.getOptionProperties("p");
    if (this.input.isEmpty()) {
      this.input = null;
    }
  }

  @Override
  public void execute() throws Exception {
    if (scriptName==null && scriptId==null) {
      log("No -n or -sid provided.  One of those two has to be specified.");
      return;
    }
    if (scriptName!=null && scriptId!=null) {
      log("Both -n and -sid are provided.  -id "+scriptId+ " will be used.");
      scriptName = null;
    }

    HttpRequest request = createHttp()
      .newPost(server + "/command")
      .headerContentTypeApplicationJson()
      .bodyObject(new StartScriptExecutionCommand()
        .scriptName(scriptName)
        .scriptId(scriptId)
        .input(input)
      );

    if (!quiet) log(request.toString("  "));

    HttpResponse response = request.execute();

    if (!quiet) log(response.toString("  "));

    StartScriptExecutionResponse startResponse = response
      .getBodyAs(StartScriptExecutionResponse.class);

    if (response.getStatus()==200) {
      log("Started script execution "+startResponse
        .getScriptExecutionId());
    } else {
      log("Error starting script execution: "+startResponse
        .getError());
    }
  }

  public String getScriptName() {
    return this.scriptName;
  }
  public void setScriptName(String scriptName) {
    this.scriptName = scriptName;
  }
  public Start scriptName(String scriptName) {
    this.scriptName = scriptName;
    return this;
  }

  public String getScriptId() {
    return this.scriptId;
  }
  public void setScriptId(String scriptId) {
    this.scriptId = scriptId;
  }
  public Start scriptId(String scriptId) {
    this.scriptId = scriptId;
    return this;
  }
}

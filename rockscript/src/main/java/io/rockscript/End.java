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

import io.rockscript.cqrs.commands.EndActivityCommand;
import io.rockscript.cqrs.commands.EngineEndActivityResponse;
import io.rockscript.http.HttpRequest;
import io.rockscript.http.HttpResponse;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Properties;

public class End extends ClientCommand {

  protected String scriptExecutionId;
  protected String executionId;
  protected Properties resultProperties;

  @Override
  protected void logCommandUsage() {
    log("rock end : Ends a waiting activity");
    log();
    logCommandUsage("rock end [end options]");
    log();
    log("Example:");
    log("  rock end se234 e8");
    log("Ends activity execution e8 inside script execution se234");
  }

  @Override
  protected Options getOptions() {
    Options options = super.getOptions();
    options.addOption(Option.builder("seid")
      .desc("Script execution id. This identifies the script execution. " +
            "Required.")
      .required()
      .hasArg()
      .build());
    options.addOption(Option.builder("eid")
      .desc("Execution id. This identifies the position of the activity " +
            "execution that will be ended inside the script execution. " +
            "Required.")
      .required()
      .hasArg()
      .build());
    options.addOption(Option.builder("r")
      .desc("Result property <propertyName>:<propertyValue>. " +
            "The property will be added to the JSON activity result object." +
            "The result object will be the return value from the activity.")
      .numberOfArgs(2)
      .valueSeparator(':')
      .build());
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
    super.parse(commandLine);
    this.scriptExecutionId = commandLine.getOptionValue("seid");
    this.executionId = commandLine.getOptionValue("eid");
    this.resultProperties = commandLine.getOptionProperties("p");
  }

  @Override
  public void execute() {
    if (scriptExecutionId==null) {
      log("No -seid provided.  It's required.");
      return;
    }
    if (executionId==null) {
      log("No -eid provided.  It's required.");
      return;
    }

    HttpRequest request = createHttp()
      .newPost(server + "/command")
      .headerContentTypeApplicationJson()
      .bodyObject(new EndActivityCommand()
        .scriptExecutionId(scriptExecutionId)
        .executionId(executionId)
        .result(resultProperties)
      );

    log(request);

    HttpResponse response = request.execute();

    log(response);

    EngineEndActivityResponse endResponse = response
      .getBodyAs(EngineEndActivityResponse.class);

    if (response.getStatus()==200) {
      log("Activity "+executionId+" in script execution "+scriptExecutionId+" ended.");
    } else {
      log("Error starting script execution: "+endResponse.getError());
    }
  }

  public String getScriptExecutionId() {
    return this.scriptExecutionId;
  }
  public void setScriptExecutionId(String scriptExecutionId) {
    this.scriptExecutionId = scriptExecutionId;
  }
  public End scriptExecutionId(String scriptExecutionId) {
    this.scriptExecutionId = scriptExecutionId;
    return this;
  }


  public String getExecutionId() {
    return this.executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public End executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }
}

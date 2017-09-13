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

import com.google.gson.reflect.TypeToken;
import io.rockscript.engine.EventsQuery;
import io.rockscript.engine.EventsResponse;
import io.rockscript.engine.impl.Event;
import io.rockscript.http.HttpRequest;
import io.rockscript.http.HttpResponse;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Events extends ClientCommand {

  protected String scriptExecutionId;

  @Override
  protected void logCommandUsage() {
    log("rock events : Queries and shows a list of events in human readable form");
    log();
    logCommandUsage("rock events [events options]");
    log();
    log("Example:");
    log("  rock events -seid se2834");
    log("Shows the events of script execution se2834 in human readable form.");
  }

  @Override
  protected Options getOptions() {
    Options options = super.getOptions();
    options.addOption(Option.builder("seid")
      .desc("Filters the events by script execution id.")
      .hasArg()
      .build());
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
    super.parse(commandLine);
    this.scriptExecutionId = commandLine.getOptionValue("seid");
  }

  @Override
  public void execute() throws Exception {
    HttpRequest request = createHttp()
      .newPost(server + "/query")
      .bodyObject(new EventsQuery()
        .scriptExecutionId(scriptExecutionId))
      .headerContentTypeApplicationJson();

    log(request);

    HttpResponse response = request.execute();

    log(response);

    EventsResponse eventsResponse = response
      .getBodyAs(new TypeToken<EventsResponse>(){}.getType());

    if (response.getStatus()==200) {
      log("Events in human readable form:");
      for (Event event: eventsResponse.getEvents()) {
        log("  "+event.toString());
      }
    } else if (eventsResponse.getError()!=null) {
      log("Events query error: "+eventsResponse.getError());

    } else {
      log("Invalid response status: "+response.getStatus());
    }
  }

}

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rockscript.activity.test.TestError;
import io.rockscript.activity.test.TestResult;
import io.rockscript.activity.test.TestResults;
import io.rockscript.engine.RunTestsCommand;
import io.rockscript.http.HttpRequest;
import io.rockscript.http.HttpResponse;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Test extends ClientCommand {

  protected String tests;

  transient protected static Gson prettyGson = new GsonBuilder()
    .setPrettyPrinting()
    .create();

  @Override
  protected void logCommandUsage() {
    log("rock test : Runs a test suite");
    log();
    logCommandUsage("rock test [test options]");
    log();
    log("Example:");
    log("  rock test -n \"*.rst\"");
    log("Runs all test scripts which have a name with .rst extension ");
  }

  @Override
  protected Options getOptions() {
    Options options = super.getOptions();
    options.addOption(Option.builder("n")
      .desc("Test script name pattern. Default is *.rst. Allowed chars are a-zA-Z_0-9*.-_/")
      .hasArg()
      .build());
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
    super.parse(commandLine);
    this.tests = commandLine.getOptionValue("n", null);
  }

  @Override
  public void execute() {
    HttpRequest request = createHttp()
      .newPost(server + "/command")
      .headerContentTypeApplicationJson()
      .bodyObject(new RunTestsCommand()
        .tests(tests)
      )
      .entityHandler((httpEntity,httpResponse)->{
        try {
          String bodyString = EntityUtils.toString(httpEntity, "UTF-8");
          if (httpResponse.isContentTypeApplicationJson()) {
            Object parsedBody = prettyGson.fromJson(bodyString, Object.class);
            bodyString = prettyGson.toJson(parsedBody);
          }
          return bodyString;
        } catch (IOException e) {
          throw new RuntimeException("Couldn't ready body/entity from http request "+httpResponse.toString());
        }
      });

    log(request);

    HttpResponse response = request.execute();

    log(response);
    log();

    if (response.getStatus()!=200) {
      log("Running the tests failed: "+response.getBodyAsString());
    } else {
      TestResults testResults = response
        .getBodyAs(TestResults.class);

      if (testResults!=null && !testResults.isEmpty()) {
        int errorCount = 0;
        for (TestResult testResult: testResults) {
          if (testResult.hasError()) {
            errorCount++;
            log("XXX "+testResult.getTestName());
            for (TestError error: testResult.getErrors()) {
              log("XXX    "+error);
            }
          } else {
            log(" ✓  "+testResult.getTestName());
          }
        }
        log();
        if (errorCount==0) {
          log("Yippie!!!  All tests passed :)");
        } else {
          log("Damn! "+errorCount+" tests failed :(");
        }
      } else {
        log("No tests were executed");
      }
    }
  }

  public String getTests() {
    return this.tests;
  }
  public void setTests(String tests) {
    this.tests = tests;
  }
  public Test tests(String tests) {
    this.tests = tests;
    return this;
  }
}

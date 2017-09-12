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

import io.rockscript.engine.DeployScriptCommand;
import io.rockscript.engine.DeployScriptResponse;
import io.rockscript.engine.ParseError;
import io.rockscript.http.HttpRequest;
import io.rockscript.http.HttpResponse;
import io.rockscript.util.Io;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

import static io.rockscript.Rock.log;

public class Deploy extends ClientCommand {

  public static final String DEFAULT_NAME_PATTERN = ".*\\.rs(t)?";

  protected boolean recursive = false;
  protected String namePattern = DEFAULT_NAME_PATTERN;
  protected Pattern compiledNamePattern;

  @Override
  protected void logCommandUsage() {
    log("rock deploy : Deploys script files to the server");
    log();
    logCommandUsage("rock deploy [deploy options] [file or directory]");
    log();
    log("Example:");
    log("  rock deploy -r .");
    log("Deploys all files ending with extension .rs or .rst ");
    log("located in the current directory or one of it's nested");
    log("directories");
  }

  @Override
  protected Options getOptions() {
    Options options = super.getOptions();
    options.addOption("r",
      "Scan directory recursive. " +
      "Default is not recursive. " +
      "Ignored if specified with a file.");
    options.addOption("n", true,
      "Script file name pattern used for scanning a directory.  " +
      "Default is *.rs  " +
      "Ignored if a file is specified. " +
      "See also https://docs.oracle.com/javase/tutorial/essential/regex/index.html ");
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
    super.parse(commandLine);
    this.recursive = commandLine.hasOption("r");
    // namePattern is already initialized with the default so
    // that Deploy also can be used programmatically.
    this.namePattern = commandLine.getOptionValue("n", namePattern);
  }

  @Override
  public void execute() throws Exception {
    String fileOrDirectory = args[args.length-1];
    File file = new File(fileOrDirectory);
    if (file.isFile()) {
      deployFile(file);
    } else if (file.isDirectory()) {
      log("Scanning directory "+file.getCanonicalPath()+(recursive?" recursive":" (not recursive)")+" for files matching "+namePattern);
      this.compiledNamePattern = Pattern.compile(namePattern);
      scanDirectory(file);
    }
  }

  private void deployFile(File file) {
    log("Deploying " + file.getPath() + " to " + server + " ...");
    try {
      String scriptText = Io.toString(new FileInputStream(file));

      HttpRequest request = createHttp()
        .newPost(server + "/command")
        .headerContentTypeApplicationJson()
        .bodyObject(new DeployScriptCommand()
          .scriptName(file.getPath())
          .scriptText(scriptText)
        );

      if (!quiet) log(request.toString("  "));

      HttpResponse response = request.execute();

      String responseBody = (String) response.getBody();
      if (responseBody.length()>100) {
        response.setBody(responseBody.substring(0,100)+"...");
      }

      if (!quiet) log(response.toString("  "));

      response.setBody(responseBody);

      DeployScriptResponse deployScriptResponse = response
        .getBodyAs(DeployScriptResponse.class);

      if (deployScriptResponse!=null && deployScriptResponse.hasErrors()) {
        log("  Errors in readable form:");
        for (ParseError parseError: deployScriptResponse.getErrors()) {
          log("  "+parseError);
        }
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void scanDirectory(File file) {
    File[] nestedFiles = file.listFiles();
    if (nestedFiles!=null) {
      for (File nestedFile: nestedFiles) {
        if (nestedFile.isFile()) {
          if (namePattern==null || compiledNamePattern
                .matcher(nestedFile.getPath())
                .matches()){
            deployFile(nestedFile);
          }
        } else if (file.isDirectory() && recursive) {
          scanDirectory(nestedFile);
        }
      }
    }
  }

  public boolean getRecursive() {
    return this.recursive;
  }
  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }
  public Deploy recursive() {
    this.recursive = true;
    return this;
  }

  public String getNamePattern() {
    return this.namePattern;
  }

  /** See {@link #namePattern(String)} */
  public void setNamePattern(String namePattern) {
    this.namePattern = namePattern;
  }

  /** A regex ( https://docs.oracle.com/javase/tutorial/essential/regex/index.html or
   * https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html ) to filter
   * the files being deployed. */
  public Deploy namePattern(String namePattern) {
    this.namePattern = namePattern;
    return this;
  }
}

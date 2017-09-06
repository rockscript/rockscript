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
import io.rockscript.http.HttpRequest;
import io.rockscript.http.HttpResponse;
import io.rockscript.util.Io;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

public class Deploy extends ClientCommand {

  public static final String DEFAULT_NAME_PATTERN = ".*\\.rs(t)?";

  protected boolean recursive;
  protected String namePattern;
  protected Pattern compiledNamePattern;

  @Override
  protected String getCommandLineSyntax() {
    return "rock deploy [deploy options] [file|directory]";
  }

  @Override
  protected Options createOptions() {
    Options options = super.createOptions();
    options.addOption("r",
      "Scan directory recursive. " +
      "Default is not recursive. " +
      "Ignored if specified with a file.");
    options.addOption("n", true,
      "Script file name pattern used for scanning a directory.  " +
      "Default is *.rs  " +
      "Ignored if a file is specified.");
    options.addOption("v",
      "Verbose.  Prints more ");
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
    super.parse(commandLine);
    this.recursive = commandLine.hasOption("r");
    this.namePattern = commandLine.getOptionValue("n", DEFAULT_NAME_PATTERN);
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
    log("Deploying "+file.getPath()+" to "+url+" ...");
    try {
      String scriptText = Io.toString(new FileInputStream(file));

      HttpRequest request = createHttp()
        .newPost(url + "/command")
        .headerContentTypeApplicationJson()
        .bodyObject(new DeployScriptCommand()
          .scriptName(file.getPath())
          .scriptText(scriptText)
        );
      log(request.toString("  "));

      HttpResponse response = request.execute();
      log(response.toString("  "));

      DeployScriptResponse deployScriptResponse = ((HttpResponse) response)
        .getBodyAs(DeployScriptResponse.class);

      if (deployScriptResponse!=null && deployScriptResponse.hasErrors()) {
        log("  Errors in readable form:");
        for (String error: deployScriptResponse.getErrors()) {
          log("  "+error);
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
  public void setNamePattern(String namePattern) {
    this.namePattern = namePattern;
  }
  public Deploy namePattern(String namePattern) {
    this.namePattern = namePattern;
    return this;
  }

  public Deploy args(String... args) {
    this.args = args;
    return this;
  }
}

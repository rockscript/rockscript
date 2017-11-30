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

import io.rockscript.api.commands.DeployScriptVersionCommand;
import io.rockscript.api.model.ParseError;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.http.client.ClientRequest;
import io.rockscript.http.client.ClientResponse;
import io.rockscript.util.Io;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

public class Deploy extends ClientCommand {

  public static final String DEFAULT_NAME_REGEX = ".*\\.rs(t)?";

  protected boolean recursive = false;
  protected String namePattern = DEFAULT_NAME_REGEX;
  protected Pattern compiledNamePattern;
  protected int deployCountSuccessful = 0;
  protected int deployCountFailed = 0;

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
      "ScriptVersion file name regex used for scanning a directory. The file " +
      "name has to end with the given name. " +
      "Default is " +DEFAULT_NAME_REGEX+" "+
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
    this.namePattern = commandLine.getOptionValue("n", DEFAULT_NAME_REGEX);
  }

  @Override
  public void execute() {
    String fileOrDirectory = args[args.length-1];
    File file = new File(fileOrDirectory);
    if (file.isFile()) {
      deployFile(null, file);
    } else if (file.isDirectory()) {
      String dirPath = getPath(file)+"/";
      log("Scanning directory " + dirPath + (recursive?" recursive":" (not recursive)") + " for files matching " + namePattern);
      this.compiledNamePattern = Pattern.compile(namePattern);
      scanDirectory(dirPath, file);
      if (deployCountSuccessful>0) {
        log(Integer.toString(deployCountSuccessful)+" scripts successful deployed");
      }
      if (deployCountFailed>0) {
        log(Integer.toString(deployCountFailed)+" script deployments failed");
      }
      if (deployCountSuccessful+deployCountFailed==0) {
        log("No scripts found to deploy");
      }

    } else {
      log("Couldn't deploy "+fileOrDirectory+".  The last argument has to be a file or directory.");
    }
  }

  private String getPath(File file) {
    try {
      return file.getCanonicalPath();
    } catch (Exception e) {
      return file.toString();
    }
  }

  private void deployFile(String dirPath, File file) {
    String scriptPath = getPath(file);
    log("Deploying " + scriptPath + " to " + server + " ...");
    try {
      String scriptText = Io.toString(new FileInputStream(file));
      String scriptName = scriptPath;

      if (dirPath!=null && scriptPath.startsWith(dirPath)) {
        scriptName = scriptPath.substring(dirPath.length());
      }

      ClientRequest request = createHttp()
        .newPost(server + "/command")
        .headerContentTypeApplicationJson()
        .bodyJson(new DeployScriptVersionCommand()
          .scriptName(scriptName)
          .scriptText(scriptText)
        );

      log(request);

      ClientResponse response = request.execute(ScriptVersion.class);

      log(response);

      ScriptVersion scriptVersion = response.getBody();

      if (scriptVersion!=null && scriptVersion.hasErrors()) {
        log("  Errors in readable form:");
        for (ParseError parseError: scriptVersion.getErrors()) {
          log("  " + parseError);
        }
        deployCountFailed++;
      } else {
        deployCountSuccessful++;
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void scanDirectory(String dirName, File file) {
    File[] nestedFiles = file.listFiles();
    if (nestedFiles!=null) {
      for (File nestedFile: nestedFiles) {
        if (nestedFile.isFile()) {
          if (namePattern==null || compiledNamePattern
                .matcher(nestedFile.getPath())
                .matches()){
            deployFile(dirName, nestedFile);
          }
        } else if (file.isDirectory() && recursive) {
          scanDirectory(dirName, nestedFile);
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

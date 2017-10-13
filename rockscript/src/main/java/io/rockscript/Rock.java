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

public abstract class Rock {

  public static void main(String[] args) {
    try {
      if (args==null
          || args.length==0) {
        logCommandsOverview();
      } else {
        String command = args[0];
        if ("help".equals(command)) {
          if (args.length>=2) {
            String helpCommand = args[1];
            CliCommand cliCommand = CliCommand.createCliCommand(helpCommand);
            if (cliCommand!=null) {
              cliCommand.logCommandUsage();
            }
          } else {
            logCommandsOverview();
          }
        } else {
          CliCommand cliCommand = CliCommand.createCliCommand(command);
          if (cliCommand!=null) {
            try {
              cliCommand.parseArgs(args);
              cliCommand.execute();
            } catch (Exception e) {
              log("Error: "+e.getMessage());
              log();
              cliCommand.logCommandUsage();
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static void logCommandsOverview() {
    logCommandsOverview(null);
  }

  static void logCommandsOverview(String invalidCommand) {
    if (invalidCommand!=null) {
      log("Invalid command: " + invalidCommand);
      log();
    }
    log("Usage: rock [command] [command options]");
    log();
    log("rock help [command]          | Shows help on a particular command");
    log("rock server [server options] | Start the rockscript server");
    log("rock ping [ping options]     | Test the connection with the server");
    log("rock deploy [deploy options] | Deploy script files to the server");
    log("rock start [start options]   | Starts a new script execution");
    log("rock end [end options]       | Ends a waiting activity");
    log("rock test [test options]     | Runs tests");
    log("rock events [events options] | Fetches events");
    log("rock                         | Shows this help message");
    log();
    log("More details at https://github.com/rockscript/rockscript/wiki/RockScript-API");
  }

  protected static void log() {
    System.out.println();
  }

  protected static void log(String text) {
    System.out.println(text);
  }
}

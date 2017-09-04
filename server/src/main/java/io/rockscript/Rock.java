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

import org.apache.commons.cli.*;

import java.util.Map;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;

public abstract class Rock {

  static Map<String,Class<? extends Rock>> COMMAND_CLASSES = hashMap(
    entry("server", Server.class),
    entry("ping", Ping.class),
    entry("deploy", Deploy.class)
  );

  public static void main(String[] args) {
    try {
      if (args==null
          || args.length==0) {
        showCommandUsage();
      } else {
        String command = args[0];
        if ("help".equals(command)) {
          if (args.length>=2) {
            String helpCommand = args[1];
            Rock rock = getRock(helpCommand);
            if (rock!=null) {
              rock.showHelp();
            }
          } else {
            showCommandUsage();
          }
        } else {
          Rock rock = getRock(command);
          if (rock!=null) {
            if (rock.parse(args)) {
              rock.execute();
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  abstract boolean parse(String[] args);
  abstract void execute() throws Exception;
  abstract void showHelp();

  private static Rock parseRock(String[] args) {
    return null;
  }

  private static Rock getRock(String command) {
    try {
      Class<? extends Rock> rockClass = COMMAND_CLASSES.get(command);
      if (rockClass==null) {
        showCommandUsage(command);
        return null;
      }
      return rockClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static void showCommandUsage() {
    showCommandUsage(null);
  }

  static void showCommandUsage(String invalidCommand) {
    if (invalidCommand!=null) {
      log("Invalid command: " + invalidCommand);
      log();
    }
    log("Usage: rock [command] [command options]");
    log();
    log("rock help [command]          | shows help on a particular command");
    log("rock server [server options] | start the rockscript server");
    log("rock ping [ping options]     | test the connection with the server");
    log("rock deploy [deploy options] | deploy scripts to the server");
    log("rock                         | shows this help message");
    log();
    log("More details at https://github.com/RockScript/server/wiki/RockScript-API");
  }

  public static void log() {
    System.out.println();
  }

  public static void log(String text) {
    System.out.println(text);
  }

  public static CommandLine parseCommandLine(Options options, String[] args) {
    CommandLineParser commandLineParser = new DefaultParser();
    try {
      return commandLineParser.parse( options, args);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }
}

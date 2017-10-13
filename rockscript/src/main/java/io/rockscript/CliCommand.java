package io.rockscript;

import io.rockscript.http.GsonCodec;
import io.rockscript.http.Http;
import org.apache.commons.cli.*;

import java.util.Map;

import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;

public abstract class CliCommand {

  static Map<String,Class<? extends CliCommand>> COMMAND_CLASSES = hashMap(
    entry("server", Server.class),
    entry("ping", Ping.class),
    entry("deploy", Deploy.class),
    entry("start", Start.class),
    entry("test", Test.class),
    entry("events", Events.class)
  );

  protected String[] args;
  protected CommandLine commandLine;

  protected abstract void execute();
  protected abstract void logCommandUsage();
  protected abstract Options getOptions();

  protected void parse(CommandLine commandLine) {
    this.commandLine = commandLine;
  }

  static CliCommand createCliCommand(String command) {
    try {
      Class<? extends CliCommand> rockClass = COMMAND_CLASSES.get(command);
      if (rockClass==null) {
        Rock.logCommandsOverview(command);
        return null;
      }
      return rockClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public CliCommand parseArgs(String... args) {
    this.args = args;
    Options options = getOptions();
    if (options!=null) {
      CommandLineParser commandLineParser = new DefaultParser();
      try {
        CommandLine commandLine = commandLineParser.parse(options, args);
        parse(commandLine);
      } catch (ParseException e) {
        throw new RuntimeException("Command line args parsing exception: "+e.getMessage(), e);
      }
    }
    return this;
  }

  protected void logCommandUsage(String usage) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(usage, getOptions());
  }

  protected Http createHttp() {
    return new Http(new GsonCodec(RockScriptGson.createCommonGson()));
  }
}

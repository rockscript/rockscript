package io.rockscript;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rockscript.http.GsonCodec;
import io.rockscript.http.Http;
import org.apache.commons.cli.*;

import java.util.Map;

import static io.rockscript.Server.createCommandsTypeAdapterFactory;
import static io.rockscript.engine.impl.Event.createEventJsonTypeAdapterFactory;
import static io.rockscript.util.Maps.entry;
import static io.rockscript.util.Maps.hashMap;

public abstract class CliCommand {

  static Map<String,Class<? extends CliCommand>> COMMAND_CLASSES = hashMap(
    entry("server", Server.class),
    entry("ping", Ping.class),
    entry("deploy", Deploy.class),
    entry("start", Start.class)
  );

  protected String[] args;

  protected abstract void execute() throws Exception;
  protected abstract void logCommandUsage();
  protected abstract Options getOptions();
  abstract void parse(CommandLine commandLine);

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


  public CliCommand parseArgs(String... args) throws ParseException {
    this.args = args;
    Options options = getOptions();
    if (options!=null) {
      CommandLineParser commandLineParser = new DefaultParser();
      CommandLine commandLine = commandLineParser.parse(options, args);
      parse(commandLine);
    }
    return this;
  }

  protected void logCommandUsage(String usage) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(usage, getOptions());
  }

  protected Gson createCommonGson() {
    return new GsonBuilder()
      .registerTypeAdapterFactory(createCommandsTypeAdapterFactory())
      .registerTypeAdapterFactory(createEventJsonTypeAdapterFactory())
      .create();
  }

  protected Http createHttp() {
    return new Http(new GsonCodec(createCommonGson()));
  }
}

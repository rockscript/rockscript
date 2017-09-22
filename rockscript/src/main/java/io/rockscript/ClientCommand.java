package io.rockscript;

import io.rockscript.http.HttpRequest;
import io.rockscript.http.HttpResponse;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public abstract class ClientCommand extends CliCommand {

  protected String server = "http://localhost:3652";
  protected boolean quiet = false;

  protected static int MAX_LOG_LENGTH = 120;

  protected Options getOptions() {
    Options options = new Options();
    options.addOption(Option.builder("s")
      .hasArg()
      .desc("The server URL.  Default value is http://localhost:3652")
      .build());
    options.addOption(Option.builder("q")
      .desc("Quiet.  Don't show the HTTP requests to the server.")
      .build());
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
    super.parse(commandLine);
    this.server = commandLine.getOptionValue("s", server);
    this.quiet = commandLine.hasOption("q");
  }

  public String getServer() {
    return this.server;
  }
  public void setServer(String server) {
    this.server = server;
  }
  public ClientCommand server(String server) {
    this.server = server;
    return this;
  }

  protected void log() {
    if (!quiet) Rock.log();
  }

  protected void log(String message) {
    if (!quiet) Rock.log(message);
  }

  protected void log(HttpResponse response) {
    if (!quiet) Rock.log(response.toString("  "));
  }

  protected void log(HttpRequest request) {
    if (!quiet) Rock.log(request.toString("  ", MAX_LOG_LENGTH));
  }
}

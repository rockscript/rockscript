package io.rockscript;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public abstract class ClientCommand extends CliCommand {

  protected String server = "http://localhost:3652";
  protected boolean quiet = false;

  protected Options getOptions() {
    Options options = new Options();
    options.addOption("s", "The server URL.  Default value is http://localhost:3652");
    options.addOption("q", "Quiet.  Don't show the HTTP requests to the server.");
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
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
}

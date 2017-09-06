package io.rockscript;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public abstract class ClientCommand extends Rock {

  protected String url = "http://localhost:3652";

  protected Options createOptions() {
    Options options = new Options();
    options.addOption("url", "The base URL of the server.  Default value is http://localhost:3652");
    return options;
  }

  @Override
  protected void parse(CommandLine commandLine) {
    this.url = commandLine.getOptionValue("url", url);
  }

  public String getUrl() {
    return this.url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public ClientCommand url(String url) {
    this.url = url;
    return this;
  }
}

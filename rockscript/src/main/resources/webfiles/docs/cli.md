tl;dr :

> `./install.sh` installs the `rock` script in `/usr/local/bin`, <br />
Then type `rock`+Enter to as the command line tool. 

The details: 

After you built the project with 

```
mvn -Pizza clean install
```

like explained in <a onclick="show('getting-started')">Getting started</a>, the command 
line client is available in the executable jar file `rockscript-cli/target/rockscript-cli.jar`.

The most basic way to use the command line client is the command
```
java -jar rockscript-cli/target/rockscript-cli.jar [command-line-options>]
``` 


But it's much cooler to create a short command for this.  Use the 
`./install.sh` to create a **`rock`** executable script in 
`/usr/local/bin/rock`.  That will reduce the above command line to 

```bash
rock [command-line-options>]
```

On Mac OS X, you can use the script `./install.sh` to create 
the `rock` executable bash script in `/usr/local/bin/rock`.  

On Linux or Unix, [let me know](https://github.com/rockscript/rockscript/issues/new) if 
the above procedure works or if you prefer a different way of installing.

On Windows, create a `rock.bat` file in any directory specified in your `%path%` environment 
variable.  And put this content in the `rock.bat`
```
@echo off
java -jar rockscript-cli/target/rockscript-cli.jar %*
```  

To get an overview of all the commands, just type `rock <Enter>` 

```
> rock
Usage: rock [command] [command options]

rock help [command]          | Shows help on a particular command
rock server [server options] | Start the rockscript server
rock ping [ping options]     | Test the connection with the server
rock deploy [deploy options] | Deploy script files to the server
rock start [start options]   | Starts a new script execution
rock end [end options]       | Ends a waiting activity
rock                         | Shows this help message

More details at https://github.com/rockscript/rockscript/wiki/RockScript-API
```

To get help on a specific command, type `rock help <command>`  Eg

```
> rock help deploy
rock deploy : Deploys script files to the server

usage: rock deploy [deploy options] [file or directory]
 -n <arg>   Script file name pattern used for scanning a directory.
            Default is *.rs  Ignored if a file is specified. See also
            https://docs.oracle.com/javase/tutorial/essential/regex/index.
            html
 -q         Quiet.  Don't show the HTTP requests to the server.
 -r         Scan directory recursive. Default is not recursive. Ignored if
            specified with a file.
 -s         The server URL.  Default value is http://localhost:3652

Example:
  rock deploy -r .
Deploys all files ending with extension .rs or .rst 
located in the current directory or one of it's nested
directories
```
#### Prerequisites

To run the server and the command line interface (CLI)
 * Java 8 SE JRE

To build RockScript
 * Java 8 JDK
 * Maven 3.3.9

### Build rockscript.jar 

Open a terminal in the root folder and run

```
mvn -DskipTests clean install
```

You eventually should see output like 

```
[INFO] rockscript-parent .................................. SUCCESS [  0.370 s]
[INFO] rockscript-gson .................................... SUCCESS [  2.752 s]
[INFO] rockscript-http .................................... SUCCESS [  0.439 s]
[INFO] rockscript ......................................... SUCCESS [ 10.176 s]
[INFO] rockscript-server .................................. SUCCESS [  6.842 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```
 
That means you have successfully created `rockscript-server/target/rockscript.jar`
 
### Starting the RockScript server

Start the RockScript server with 

```
java -jar rockscript-server/target/rockscript.jar
```

You should see output like this

```
 ____            _     ____            _       _    
|  _ \ ___   ___| | __/ ___|  ___ _ __(_)_ __ | |_  
| |_) / _ \ / __| |/ /\___ \ / __| '__| | '_ \| __| 
|  _ < (_) | (__|   <  ___) | (__| |  | | |_) | |_  
|_| \_\___/ \___|_|\_\|____/ \___|_|  |_| .__/ \__| 
                                        |_|         
Server started on port 3652
```

**Limitation**: Bear in mind that for now, the server only has an in-memory event store.
So each time you reboot the server, it looses all it's scripts and script executions._

To check that you have the server running, you can just check open 
the documentation that is included in the server. Point your browser 
to [http://localhost:3652/](http://localhost:3652/)

Next, take <a onclick='tutorial'>the 5 minute tutorial</a> and get your first 
script running.

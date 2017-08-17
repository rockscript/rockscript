![RockScript](docs/image/logo.png)

 * __Resilient script execution__
 * __Support for including long running activities__  
 * __Used for microservice orchestration__
 * __Valuable component in event driven architectures__
 * __Easier way to write reactive code__

### Why

The syntax of RockScript is based on JavaScript, so it will be very familiar 
to you.
  
RockScript adds the notion of an **Activity**.  In the script, an activity looks 
like a function invocation but they are executed asynchronously by an 
**Activity runner** component.  Activities can be long running: from milliseconds, 
to days or even months.  The script execution will not block any thread or 
memory resources while it is waiting for an activity completion callback.
So you can write reactive code without the typical callback hell or other 
synchronization hassles as in other programming languages.   

But the cool part is that executions are resilient.  The runtime state of 
each script execution is stored with event sourcing as the script executes.
So script executions can _survive RockScript server crashes_.  It's designed 
so that a group of RockScript servers can form a cluster. 

### When

**Event listeners**: RockScript is ideal for implementing event listeners in 
an event driven architecture.  That's because your server often needs to perform 
multiple integration activities in response to an incoming event.  Often 
these integrations happen over HTTP and cannot participate in a transaction.  
If you code these integrations in other programming languages, execution is not 
crash-proof.  So the overall system may get inconsistent if your server crashes 
half way during the integration activities.  You also don't have a way  to 
know which scripts have crashed and at which point they stopped.  

RockScript ensures that script executions recover from server crashes and from 
the stored runtime script execution state you can analyse what has happened.
This capabilities make RockScripts a viable alternative for transactions.

**Long running activities**: RockScript allows you to write scripts that 
are include long running activities like document transformations, 
user tasks, waiting for clicking a confirmation link, report generations, 
big file transformations, long running map reduce jobs, etc.  Also any API 
operation can be wrapped as a RockScript activity.

### Usage by example

Prerequisites:

 * Java 8
 * Maven 3.3.9 (to build the engine)
 * NodeJS 7.7.1 (to run the example)

Build the engine with 

```
mvn -Pizza clean install
```

Start the server with 

```
java -jar server/target/rockscript.jar
```

You should see output like this

```
59,868 INFO Server -  ____            _     ____            _       _    
59,868 INFO Server - |  _ \ ___   ___| | __/ ___|  ___ _ __(_)_ __ | |_  
59,869 INFO Server - | |_) / _ \ / __| |/ /\___ \ / __| '__| | '_ \| __| 
59,869 INFO Server - |  _ < (_) | (__|   <  ___) | (__| |  | | |_) | |_  
59,869 INFO Server - |_| \_\___/ \___|_|\_\|____/ \___|_|  |_| .__/ \__| 
59,869 INFO Server -                                         |_|         
00,021 INFO Server - Server started on 8888
```

Open a new console and look at the contents of the `joke.rs` script

```
> cd docs/examples/jokes
> more joke.rs 
var http = system.import('rockscript.io/http');
var jokes = system.import('localhost:3000');

var chuckResponse = http.get({url:'http://api.icndb.com/jokes/random'});

jokes.joke(chuckResponse.body.value.joke);
```

The `http` activity worker is built into the engine itself.  

The `jokes` activity worker is an example service for getting a laugh from jokes.  
It shows 2 aspects: 1) That you can make activity worker available over http, and
2) That activities can take a long time to complete.

The idea is that you can add a joke to the service.  And a joke ends when someone 
has laughed with the joke.

Start the `joke-server.js` with the command

```
> node joke-server.js 
Joke Server listening on port 3000!
```

Now point your browser to `http://localhost:3000`  

To add the first jokes, we run the  
Deploy the `joke.rs` script to the RockScript server with the following command

```
> curl -X POST --data-binary @joke.rs localhost:8888/scripts
{"id":"s1"}
```

The response is the script id.

```
> curl -X POST -H "Content-Type: application/json" -d {startScript:{scriptId:"s1"}} localhost:8888/command
```


### Project stage

RockScript is experimental stage and very early in its development.

**RockScript does not offer any stability guarantees at this point.**
  
We are looking for feedback and use cases.  

[Create an issue](https://github.com/RockScript/server/issues/new) for 
any question, suggestion or other feedback.  We really appreciate it.

### License

The RockScript runtime server is Apache License V2.  

### Commercial offerings

**RockScript consulting** gets you in depth expertise help you use RockScript in your project.  We have 
discount pricing for consulting that helps you evaluate if RockScript is a good fit.  

The **RockScript Devtool** is a web based tool that provides convenience for developers.  This tool is 
free, but not open source.  You have to register and provide us with your contact details in order 
to get it.  (planned)

The **RockScript Manager** is a commercial extension of the dev tool to monitor and administer 
production RockScript servers.  (later)

**RockScript Service** is a SaaS version of the server and includes development tool, 
monitoring and administration.  (later)  

### Alternatives

RockScript is an alternative for 

 * [AWS step functions](https://aws.amazon.com/step-functions/)
 * [Netflix conductor](https://github.com/Netflix/conductor)
 * [Microsoft logic apps](https://azure.microsoft.com/en-us/services/logic-apps/)

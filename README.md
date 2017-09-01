![RockScript](docs/img/logo.png)

## What is it?

RockScript creates a new category of programming languages.  Most programming 
languages differ in their syntax or style.  RockScript offers a different 
execution engine that enables non-blocking waiting which is resilient and 
resumable.  Read on to learn more.

To get an impression, read the 5 minute 
**[Tutorial by example](https://github.com/RockScript/server/wiki/Tutorial-by-example)**

#### RockScript is based on JavaScript syntax

The syntax of RockScript is based on JavaScript, so it will be very familiar 
to you.

#### RockScript adds support for long running activities
  
RockScript adds the notion of an [Activity](https://github.com/RockScript/server/wiki/Activities). In 
the script, an activity looks like a function invocation but they are executed asynchronously 
by an _Activity worker_ component.  Activities can be long running: 
from milliseconds, to days or even months.  The script execution will not block any thread 
or memory resources while it is waiting for an activity completion callback.
So you can write reactive code without the typical callback hell or other 
synchronization hassles as in other programming languages.   

## Why use it?

#### Resilient script execution

But the cool part is that executions are resilient.  The runtime state of 
each script execution is stored with event sourcing as the script executes.
So script executions don't consume resources like threads and memory when 
they are waiting for asynchronous activities to complete.  Also because 
the script executions are stored, you can fully inspect what happened when 
something goes wrong like a server crash and even recover from the last 
saved execution position.  It's designed so that a group of RockScript 
servers can form a cluster.

#### Juggle with JSON

When combining multiple microservices interactions, you often need to 
transform the data between those interactions.  Most of the microservices 
interactions are based on JSON. There is no better language to deal with 
JSON manipulation than JavaScript.  That's why RockScript is based on 
JavaScript.  So that coding your data transformations between microservice 
interactions becomes super easy. 

#### More readable then reactive code with callbacks

Even though activities look like normal function calls, they are executed 
asynchronous.  This means that with RockScript, you do not require any of 
the complex synchronization constructs like callbacks, futures, promises
to get asynchronous execution.

#### Full stack JavaScript

There are already solutions for writing JavaScript frontend and backend.
  
RockScript adds a crucial integration component so that developers can deliver 
complete applications in one language.  Because JavaScript is the most 
common language, it will be easy to train and find developers so it's a 
safe technology choice.

## When to use it?

#### Resilient microservice workflows

Interactions with microservices are mostly done in JSON over HTTP.
These interactions therefor can not participate in a transaction.
At any point during an interaction, your server or the called 
microservice may crash.  In order to keep consistency when combining 
multiple microservice interactions you need to keep very good track of 
which service you started calling, which already completed.  And how will 
you recover?  That is exactly what RockScript will do for you.

#### Event driven architectures

RockScript is designed to be connected with messages as well as HTTP 
API. (This is still planned, not yet implemented).

TODO: Describe how RockScript has the vision of applying the Triggers and Actions 
concepts (similar to IFTTT) to event driven microservice architectures.

RockScript is ideal for implementing event listeners in 
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

## Project status

RockScript is experimental stage and quite early in its development.  

**RockScript does not offer any stability guarantees at this point.**

You can help us with your feedback.  [Create an issue](https://github.com/RockScript/server/issues/new) for 
any question, suggestion or other feedback.  We really appreciate it.
 
I'm planning to build a business model on top of RockScript.  I'm interested to find 
a cofounder.  If you think you have what it takes (don't be shy) email me.  
 
The current business plan is based on:

**RockScript Consulting** gets you in depth expertise to help you use RockScript 
in the best way for your project.  

**RockScript Service** is a SaaS version of the server with the UI to deploy, test, 
continuously integrate, monitor and administer the server. (planned)  

**RockScript On-Premise** is an on-premise version of the SaaS product that can be installed
on your own systems. (later)  

## License

The full RockScript runtime server is Apache License V2.  

The commercial tooling will not be open source.

## Thanks

Thanks to [GitHub](https://github.com) for hosting the code, community and website.

Thanks to JetBrains for their support to open source and the free [Intellij IDEA Ultimate](https://www.jetbrains.com/idea/) license.

Thanks to [Travis CI](https://travis-ci.org/) for their continuous integration

## Alternatives

RockScript is an alternative for 

 * [AWS step functions](https://aws.amazon.com/step-functions/)
 * [Netflix conductor](https://github.com/Netflix/conductor)
 * [Microsoft logic apps](https://azure.microsoft.com/en-us/services/logic-apps/)
 * BPM and workflow

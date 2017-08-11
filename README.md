<div style="text-align: center;">
![RockScript](docs/image/logo.png)
</div>

<div style="text-align: center;">
__Orchestrate microservices with event driven architecture.__
</div>

### Why

RockScript is based on JavaScript.  But the cool part is the 
runtime engine.  All side effects of imported functions are 
executed asynchronously.  The state of script executions can 
be serialized and persisted with event sourcing.  This enables 
developers to write event driven architectures without complex 
synchronization hassles.

### Usage

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
```

Next create a file called `convert-quote.rs` with the following contents

```javascript
var http = system.import('rockscript.io/http');
var requestbin = system.import('rockscript.io/requestbin');

var quotes = http.get({ url: 'http://api.fixer.io/latest' });

requestbin.createBin({
  content: '1 EUR is '+quotes.body.rates.USD'
})
```

### Why

Here we list the features of zooscript that you will not find in other programming 
or script languages.

* **Simplifies and jump starts your event based architecture**: Write synchronous code and 
    get asynchronous execution. 
    Futures are an ugly hack around a shortcoming of the language. In zooscript, we have a 
    language  primitive that can wait until an external event is received with a callback.  
    Functions can be created that include non-blocking waits.  During waits, the server does 
    not consume resources like memory or threads.
* **Extreme visibility**: You can track all the details of the execution of controller scripts 
    because the complete execution of the TBN scripts are stored with event sourcing.  Without 
    adding logging to your code you can trace every part of the execution up to a very detailed 
    level.  Even in production.  Also you can monitoring the performance of all external services 
    your service uses.  
* **Continue execution after a crash**: If your script execution crashes, it will be picked up 
    and resumed by another server _from where it crashed_.
* **Cloud scalability**: This project aims to provide a easy deployment environment for 
    lambda-like cloud functions.
* **Transaction alternative**: Because the script is executed persistent, it's ideal as a 
    to replace transactions in a HTTP microservices context.  It remembers which requests 
    have been started and which have been completed.  So you can be sure that all 
    requests will be performed eventually.  You will have to take into account idempotence of 
    interactions with other services, but there is no other alternative for the interprocess 
    communication over HTTP.
* **Step back in debugger**: Also because the script engine is build on event sourcing, 
    it can step back in time.  So you can replay events up to an earlier time and resume 
    from there
* **Architectural guidance**: First wrap the external service APIs that you want to use 
    as a set of actions and triggers.  Then secondly compose the script at a higher level.
    This is a great way to divide and conquer a large software project.

### Project stage

RockScript is very early in its development and is does not offer any stability guarantees at this point.
  
We are looking for feedback and use cases.  Contact us if you're in doubt if RockScript is the
right solution for your project.

### License

The RockScript runtime server is Apache License V2.  

We also make the commitment that there will only be one version of the runtime server: the open source 
one.  All its capabilities will be included in open source.  The open source engine will not be cripled 
so that you are not forced to buy a commercial version.  Our business model is based on services and tooling 
on top of the open source runtime engine.

### Commercial offerings

**RockScript consulting** gets you in depth expertise help you use RockScript in your project.  We have 
discount pricing for consulting that helps you evaluate if RockScript is a good fit.  

The **RockScript Devtool** is a web based tool that provides convenience for developers.  This tool is 
free, but not open source.  You have to register and provide us with your contact details in order 
to get it.  (coming soon)

The **RockScript Manager** is a commercial extension of the dev tool to monitor and administer 
production RockScript servers.  (coming later)

**RockScript Service** is a SaaS version of the server and includes development tool, 
monitoring and administration.  (coming later)  

### Related technologies

RockScript is an alternative for 

 * [AWS step functions](https://aws.amazon.com/step-functions/).
 * Netflix conductor
 * Microsoft logic apps
 

It's similar to [OpenWhisk](http://openwhisk.org/) in terms of many concepts and offering an 
architectural basis for on reactive, event based systems.

It's like [IFTTT](https://ifttt.com/), but for developers.  Instead of a UI, it offers a 
scripting language to work with actions and triggers.  Actions and triggers are 
easy configurable interactions with an external API offers.

====
Deprecated
====


### Example usage scenarios

Eg The UI might have a user registration form.  The UI spits out an event 
UserRegistrationFormSubmitted.  The logic that has to be performed in reaction 
to that event can be written in zooscript.

Eg Later, there might be a backend-only example when the user has been created 
in the identity service.  The identity service could publish an event called 
UserCreated.  Many backend systems might have to be informed and they all may 
need to do some internal things with this event.

### Architectural picture

A zooscript typically belongs to a service and contains the logic of how this service 
reacts on an event in another system.

### How does it work


![Mapping an API](img/api-mapping.png)

### Triggers

A trigger represents an event that an API emits that can be used to start 
an action script execution. 

Examples

* A file is uploaded in some folder
* A database record is updated
* 

### Actions

An *action* represents an interaction with an API to an external system.  
It can be a single HTTP request or a more complex message exchange pattern.
Actions can potentially take a long time to complete.

The action script execution engine has the ability to wait asynchronous for 
an action to complete.  During that waiting time, the execution is persisted 
and it will be resumed when the action indicates it's done.

Examples of actions are:

* Creation of a pdf document
* Sending an email
* An approval performed by a person
* Perform a credit rating
* Any REST API request

### Action script

Action script coordinates actions.  

Script executions are persisted and it has an easy syntax for parallel execution, 
resulting in 4 main benefits:

1) **Crash recovery**:  If the server crashes in the middle of an execution,
   the execution will be recovered and resumed from the point where it 
   crashed.  This is an alternative for transactions on cloud infrastructures
   because it gives eventual consistency.

2) **Scalability**: A script execution only needs to be in memory when 
   it is performing the logic between the actions.  Whenever actions are 
   performed, the script execution is stored in a database (event store).
   So script executions do not take up CPU or memory resources when they 
   are waiting for actions to complete.

3) **Simplified concurrency**: Especially when applying event driven patterns, 
   enforcing a combination of sequential and parallel execution can become 
   incredibly difficult.  Action script has a very simple way to specify when 
   actions can be executed in parallel.  And the engine will deal with 
   the coordination of when to invoke which action next.

4) **Visibility**: The persistence of script executions is based on event 
   sourcing.  This means that you have detailed information for each 
   execution for debugging or audit for the complete lifetime of the script 
   execution.  This includes start and end times of the whole execution, 
   as well as start and end times of the actions, variable and parameter 
   values and so on.  And since script executions are stored in a database, 
   it is easy to query statistics like eg how long does this action or script 
   take on average.

### Action handler 

An action handler acts as a bridge between the script and the external service.
It offers a single logical operation to an external service as a function in the 
action script.  Each time the script engine 
has to execute an action, the script engine sends a notification (and optionally 
input data) when the action starts.  And the script engine expects a callback 
from the action handler when the action completes.

![Action handler interaction](img/action-handler.png)

### Example script

```JavaScript
var http = import('tbn.io/core/http');
var accounts = import('myorg.io/accounts');
var crm = import('myorg.io/crm');

var email = execution.input;

var emailResponse = http.post({
  url: 'http://'+server+'/email/send',
  headers: {
    'Authorization': oauth.getAccessToken('Email service')
  },
  body: 'Hi, please click this link...',
  expectedStatus: 200,
  maxRetries: 3
});

var confirmationForm = execution.waitForCallback('Confirmation form');

[ // statements in this square-bracket-block 
// are executed in parallel
accounts.createUser({
  name: confirmationForm.name,
  email: email,
  passwordHash: confirmationForm.passwordHash
});

crm.addUser({
   email: email,
   name: confirmationForm.name
});
]
```

`http`, `accounts` and `crm` are action handlers.

`execution` is the object representing the execution which is used for the following 
* obtain input data 
* wait for an external callback
* set output data

Scenario:
This script can be started by the UI when the user submits an email address.

Next, a `http.post` is performed to some internal service that sends the 
registration confirmation email with a link.

Then the script execution will wait in `execution.waitForCallback` 
until the user completes the confirmation and the UI calls back 
to the script engine signalling the script can continue and passing 
the `confirmationForm` as data. 

Next the user is created in the user service and the user is also added to the 
crm system in parallel.


# Use cases

### TODO delete these step function use cases

* Document and Data Processing - Consolidate data from multiple databases into unified reports. Refine and reduce large data sets into useful formats.
* DevOps - Build tools for continuous integration and continuous deployment. Create event-driven applications that automatically respond to changes in infrastructure.
* E-commerce - Automate mission-critical business processes, such as order fulfillment and inventory tracking.
* Web Applications - Implement robust user registration processes and sign-on authentication.

### TODO delete these netflix conductor motivations

* Allow creating complex process / business flows in which individual task is implemented by a microservice.
* A JSON DSL based blueprint defines the execution flow.
* Provide visibility and traceability into the these process flows.
* Expose control semantics around pause, resume, restart, etc allowing for better devops experience.
* Allow greater reuse of existing microservices providing an easier path for onboarding.
* User interface to visualize the process flows.
* Ability to synchronously process all the tasks when needed.
* Ability to scale millions of concurrently running process flows.
* Backed by a queuing service abstracted from the clients.
* Be able to operate on HTTP or other transports e.g. gRPC.

### Crash resistent script execution

Execution state of TBN scripts is stored through event sourcing.
At any point during execution, the state is persisted.  The execution can resumed 
from the persisted events.

So if your system crashes while executing a script, the script execution can 
resume from the point where it crashed when the TBN server boots up again.
 
In a clustered deployment, an execution can even resume on another node in the 
cluster.

### Transaction alternative for microservices

HTTP requests can not participate in transactions.  So the only thing you can do is
track when you start a request and store the result when a request finishes.

When the script server crashes, another server can pick up and resume the execution
of the script at the point where the execution crashed.

This way, you are sure that your script completes eventually and that
all the services will be called sucessfully at least once.

### Coordination of microservices and people

Consider a bunch of microservices and people.  TBN is ideal when you are implementing a 
service that represents an execution flow spanning over other services and people.

TBN makes abstraction of the interaction pattern that you use to communicate with 
external systems.  Be it synchronous HTTP invocation, HTTP send and HTTP callback 
or asynchronous messages.  In the TBN script all these interaction patterns will 
just appear as function invocations.

In an execution flow, there is always one system or person responsible for making 
progress.  When an external system or person becomes responsible, the TBN script 
will wait until the external entity is done and a callback is received.  During 
the wait, the execution state is stored persistent so that no thread or 
memory resources are being consumed while waiting.

### Long running functions

Imagine the scenario of registering an account online.  A snippet of
the script could look like this:

```
emailService.send({
  to: [registrant.email],
  subject: 'Confirm registration',
  body: 'Please click this link http://...'+registrant.code'
});
var confirmationForm = taskService.completeConfirmationForm();
identityService.createUserAccount(confirmationForm.email);
```

The main idea I want to show with this code is that TBN allows you to
work with functions that span a long time.



Function invocations that potentially take a long time are split into
starting the function and completing it asynchronous.

### Avoid callback hell

In async architectures, you are forced to work with functions or lamda's as callbacks.
It's easy to loose the overview. For example:

```
function sendUpdateRequest(payload) {
  messageQueue.send('someQueue', payload);
}

function receiveUpdateResponse(response) {
  // do stuff with the response
}
```

When code has a lot of these callbacks

TBN scripts allow you to write that overview
in a much more readable way.

Write
```
var updateResponse = updateService.invokeUpdate(payload);
// do stuff with the response
```

Instead of writing

#### Parallel and sequential execution
It's easy to specify which requests have to be performed
in parallel and which have to be performed in sequence.
The script engine will keep track of synchronization.

#### Retries and error handling
A key aspect of building cloud systems is error handling,
retries and failover.  This is cumbersome code to write
in any programming language.  The script language has good defaults
allows to configure specific timeouts, retries and failover
behavior.  This makes it easy to build resilient
applications on the cloud.

#### Easy readable script language
The script language requires the least amount of input
for the execution to work.  Scripts are easy to write
by hand and easy to read.
Language drivers provide builders that give compile time
guidance to create workflow scripts.

#### Pluggable
Extend the script language by defining your own custom activities.

#### Scalable
The script engine is designed to run in public and
private cloud environments and is build on proven scalable
cloud technologies.  All HTTP interactions are done with
non blocking io for optimized performance.

#### Testable
Scripts can be tested like any other piece of software.
You can do this from your own language like Java or we also
have a test runner that is able to run test specifications
like this.

## History

This project has a link with workflow.  The founders have a long track record in 
workflow systems, but realized that workflow was not ideal for developers.
Workflows have a visual representation usually of boxes and arrows.  On the other 
hand workflows represent an execution flow and can have wait states.  We realized 
that developers do not need the visual diagram representation.  Writing and maintaining 
workflow scripts becomes a lot easier when offering the underlying features of workflow 
systems as a scripting langauge.

### TODO identify remarkable aspect

Inspired by the TED podcast about "How things spread".  For things to spread, 
you need a remarkable aspect.  Think about the video series "Can we blend it?"
Where people blend things like iphones and other stuff.  It's to show to others
that you have a sense of humor.  We need something remarkable.  

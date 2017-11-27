#### Resilient script execution 

Most system interactions these days are done over HTTP and are non 
transactional.  Between the time you start sending a request and the 
time you have received the confirmation in the response, you can't really
know the state of the service you tried to invoke.  

The only thing you can do is keep track of which request you started 
and which requests completed successfully.  That is exactly what 
RockScript does for you.  RockScript keeps track of which interactions 
have started, which have completed and the complete runtime execution 
state with event sourcing.   

That means RockScript is also able to recover from crashes, something 
that normal programming languages cannot do.  To get resilience without
RockScript, you have to revert to message queues, which require 
you to cut or your code and connect those pieces with message queues.  
With message queues, the overall business logic gets really hard to 
follow and test.  With RockScript it's easy to read your business logic 
in the scripts and test them.  

Another very interesting consequence of storing the execuiton state with 
event sourcing is that you can really inspect the complete execution flow during 
and after the execution finishes.  Imagine that you can investigate production
issues with [the exeuction inspector](http://rockscript.io/products/#webui) 
that lets you see what happened like in a debugger.  

#### Juggle with JSON

When combining multiple microservices interactions, you often need to 
transform the data between those interactions.  Most of the microservices 
interactions are based on JSON. There is no better language to deal with 
JSON manipulation than JavaScript.  That's why RockScript is based on 
JavaScript.  So that coding your data transformations between microservice 
interactions becomes super easy. 

#### Write blocking code, get non-blocking execution

With asynchronous messaging, you're forced to cut up your code in pieces and 
connect the code-pieces with message queues, configuration and infrastructure. 
It can be a real challenge to read or debug code like that.

Activities package an interaction with an external system as a simple function 
invocation. Activity invocations look like normal function invocations.  They are 
familiar and simple to read.  But unlike other programming languages, the RockScript 
engine can execute those activity invocations non-blocking.

With RockScript, it's much easier to keep the overview.  The script contains the 
essence of the business logic.  All the communication details are 
handled by the activity workers.  Because those details are extracted from the 
script, it's orders of magnitude easier to read, write and maintain compared to 
messages and message handlers.  

RockScript lets you use a style of coding that is familiar to all developers.
So it's a safe choice in larger teams or teams where churn can be expected. 

#### Alternatives

The most used alternative is message queues.  This is a lower level solution that requires 
you to cut your code into pieces and tie your code-pieces together with with message 
queues and configuration of that infrastructure.  It gets really hard to distill the 
business logic from all those fragments and configurations.  RockScript let's you 
write scripts at a higher business logic level and activity workers are a more elegant 
approach to breaking down the lower level communication details. 

Other solutions on the same level as RockScript are  
* [AWS step functions](https://aws.amazon.com/step-functions/)
* [Uber Cadence](https://github.com/uber/cadence)
* [Netflix conductor](https://github.com/Netflix/conductor)

All these alternatives are based on tasks, which are similar to our activities. But there 
is an important difference related to coding the execution flow between these activities.  
In RockScript, the control flow is defined in the script itself.  This means constructs 
like `if (condition) {...} else {...}`, `for (loop) {...}` and blocks implying sequential 
execution.  The script, written in JavaScript syntax contains the activity invocations as 
well as the control flow logic between the activities.

In AWS Step Functions and Uber Cadence the logic between the activities has to be 
implemented in the form of callbacks.  Each time an activity is finished, those engines will 
call out to a workflow callback that has to calculate the next activity.  So in these 
solutions you don't have an overview.  While you can code that logic in any language as well,
you have to cut up your code in pieces.  This way it's harder to see the overview of 
how tasks are connected and what transformation logic is done inbetween.

Other similar alternative technologies are 
[Microsoft logic apps](https://azure.microsoft.com/en-us/services/logic-apps/), 
BPM and workflow.
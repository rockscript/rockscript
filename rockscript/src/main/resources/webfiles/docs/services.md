Services are made available to scripts through `system.import` as 
explained in <a onclick="show('language')">Language</a>.

There are 2 built-in services at the moment (We hope to expand this 
list. Your contribution can help)

* HTTP service: perform plain HTTP requests
* Test service: Functions for starting scripts, ending service functions and 
  asserting conditions.

To plug in your own service, you don't need to configure anything.  
Just host a service bridge that listens to the HTTP POST requests described 
below.

In the script, import the service by specifying the url like this

```
var yourService = system.import('yourhost:yourport/yourpath');
...
yourService.yourServiceFunction('your function', args);
```

Each time `yourServiceFunction` is executed, a POST request like this will 
be performed:
```
POST http://yourhost:yourport/yourpath/{functionName}
Content-Type: application/json
{ scriptExecutionId: "se98237",
  executionId: "e9",
  args: ['your function, [whateverTheContentOfArgsWas]]
}
```

Your service bridge can respond to this POST request in 1 of 2 ways: 

**1) I have received the notification to start the serviceFunction, I'll call you back when it's done.**

Then the response body can be empty and should return status `200`.

Later, when the service bridge has finished the work for the function, the service bridge 
should call back to the server with a POST request like this:

```
POST http://RockScriptServer:port/command
Content-Type: application/json
{ endFunction: {
    scriptExecutionId: "se98237",
    executionId: "e9",
    result: {
      examplePropertyOne: "hello",
      examplePropertyTwo: "world",
    }
}
```

The result is the return value.  It's optional and can be any valid json value.  
If the `Content-Type` is set to `application/json`, then the result is parsed as 
JSON and the JSON value will be made available as a JSON object.

For any other `Content-Type`, a string is returned as the return value of the 
service function.

**2) The service function is already done, please continue the script.**

Then the response should look like this:

```
200 OK
Content-Type: application/json
{ ended: true,
  result: "any JSON value" 
}
```

The service function completes immediately and the result is passed as the 
return value and the script continues.

This approach 2) should not be done for service functions that take a long time 
because the engine will perform the initial request blocking.  To implement 
non-blocking server execution of your service function, use approach 1) above.   
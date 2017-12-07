Commands are RPC style operations that change the state of the RockScript server.
Commands are invoked by a HTTP POST to `http://localhost:3652/command` with the command
JSON as the body.  We hope to add message based API later.
Commands have a the following JSON serialization form
(aka [wrapper object](https://fasterxml.github.io/jackson-annotations/javadoc/2.9/com/fasterxml/jackson/annotation/JsonTypeInfo.As.html#WRAPPER_OBJECT)):
```
{"commandName": { ...command data fields... }}
```
Example of a command request/response:
```
> POST http://localhost:3652/command
  Content-Type: application/json
  { "saveScript":
    {
      "scriptName": "My first script",
      "scriptText": "var v = 42;"
    }
  }

< HTTP/1.1 200 OK
  Access-Control-Allow-Origin: *
  Content-Type: application/json
  { "id":"sv1",
    "scriptId":"s1",
    "name":"My first script",
    "version":1,
    "text":"var v = 42;"
  }
```

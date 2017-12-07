Get all the details of a single script execution.

## Example
Here's an `execution` query example
 
```
> GET /query?q=execution&id=se1
< HTTP/1.1 200 OK
  Content-Length: 1465
  Access-Control-Allow-Origin: *
  Date: Thu, 07 Dec 2017 08:53:25 GMT
  {
    "events": [
      {
        "scriptStarted": {
          "time": "2017-12-07T08:53:25.784Z",
          "scriptExecutionId": "se1",
          "scriptId": "s1",
          "scriptVersionId": "sv1",
          "scriptName": "Unnamed script",
          "scriptVersion": 1
        }
      },
      {
        "variableCreated": {
          "time": "2017-12-07T08:53:25.792Z",
          "scriptExecutionId": "se1",
          "executionId": "e1",
          "line": 1,
          "variableName": "simple",
          "value": "import(\u0027rockscript.io/simple\u0027)"
        }
      },
      {
        "serviceFunctionStarting": {
          "time": "2017-12-07T08:53:25.793Z",
          "scriptExecutionId": "se1",
          "executionId": "e7",
          "line": 2,
          "serviceName": "rockscript.io/simple",
          "functionName": "wait",
          "args": {}
        }
      },
      {
        "serviceFunctionWaiting": {
          "time": "2017-12-07T08:53:25.794Z",
          "scriptExecutionId": "se1",
          "executionId": "e7",
          "line": 2
        }
      }
    ],
    "scriptVersion": {
      "id": "sv1",
      "scriptId": "s1",
      "scriptName": "Unnamed script",
      "version": 1,
      "active": true
    },
    "id": "se1",
    "variables": {
      "system": {
        "import": "import"
      },
      "simple": "import(\u0027rockscript.io/simple\u0027)"
    },
    "serviceFunctionContinuations": [
      {
        "id": "e7",
      "functionName": "rockscript.io/simple/wait",
      "args": []
    }
  ],
  "start": "2017-12-07T08:53:25.784Z"
}
```

## Request query parameters

| Property name | Required? | Type | Description |
|---|---|---|---|
| `q` | Required | String | `q=execution` specifies this is an query for script execution data |
| `id` | Required | String | The ID of the script execution |

## Response properties

See example above

Us the command line interface to get a human readable version of the events. 

## Failures

The request returns a `400 BAD REQUEST` in the following situations

TODO

In each of these, a json body will be returned with an error message.  For example 

```
HTTP/1.1 400 BAD REQUEST
Content-Type: application/json
{ "message": "...error message..." }
```
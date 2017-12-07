Ends a waiting function invocation in a script execution.

### Example
Here's a `endFunction` example
 
```
> POST /command
  Content-Type: application/json
  { "endFunction" :
    { "scriptExecutionId" : "se1",
      "executionId" : "e46"
    }
  }
< HTTP/1.1 200 OK
  Access-Control-Allow-Origin: *
  { "id":"sv1",
    "scriptId":"s1",
    "scriptName":"Test script",
    "version":1,
    "text":"var a\u003d0;",
    "active":true
  }
```

### Request properties

| Property name | Required? | Type |Description |
|---|---|---|---|
| `scriptExecutionId` | Required | String | ID that identifies the script execution |
| `executionId` | Required | String | ID that identifies the function invocation which is a position within the script execution |
| `result` | Optional | Any JSON value | The return value coming out of the function invocation |

### Response properties

| Property name | Data type | Description |
|---|---|---|
| `scriptExecutionId` | String | ID of the script execution |

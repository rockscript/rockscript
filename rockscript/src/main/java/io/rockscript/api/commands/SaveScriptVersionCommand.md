
Creates a script version on the server.  

### Example
Here's a `saveScript` example
 
```
> POST /command
  { "saveScript" :
    { "scriptName" : "Test script",
      "scriptText" : "var a\u003d0;",
      "activate":true
    }
  }
< HTTP/1.1 200 OK
  Access-Control-Allow-Origin: *
  { "id":"sv1",
    "scriptId":"s1",
    "name":"Test script",
    "version":1,
    "text":"var a\u003d0;",
    "active":true
  }
```

### Request properties

| Field name | Description |
|---|---|
| `scriptName` | Name that identifies the script for which this command creates a new version |
| `scriptText` | The script text |
| `activate` | If set to true, this new version will become active, which means that new script executions will start in the new version.  <br/>Default is false. |

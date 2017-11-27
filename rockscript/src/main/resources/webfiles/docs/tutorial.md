### Prerequisites

 * A running RockScript server.  See <a onclick="show('getting-started')">Getting started</a> 
 * NodeJS 7.7.1+

### Your first script

```javascript
var http = system.import('rockscript.io/http');

var response = http.get({url:'http://api.icndb.com/jokes/random'});

var joke = response.body.value.joke;
```

`http.get(...);` in line 4 will perform a HTTP request and makes 
a HTTP response object available in the script.  For this particular URL,
the response object looks like this:

```json
{ "status": 200,
  "headers": { 
    "Transfer-Encoding": ["chunked"],
    "other": "irrelevant properties"
  },
  "body": {
    "type": "success",
    "value": {
      "joke":"...a Chuck Norris joke...",
      "other": "irrelevant properties"
    }
  }
}
```

### Deploying the script

Paste this into the command line to save your first script version
 
```bash
$ curl -v -H "Content-Type: application/json" \
  -d "{\"saveScript\":{ \
    \"scriptName\": \"Get joke\", \
    \"scriptText\": \"var http = system.import('rockscript.io\/http');\r\n\r\nvar response = http.get({url:'http:\/\/api.icndb.com\/jokes\/random'});\r\n\r\nvar joke = response.body.value.joke;\", \
    \"activate\": true \
  }}" \
  http://localhost:3652/command
```

You should see a response like this:

```bash
{"id":"sv2","scriptId":"s1","name":"Get joke","version":1,"text":"var http..."}
```

### Starting the script 

```bash
curl -v -H "Content-Type:application/json" -d "{\"startScript\":{\"scriptName\":\"Get joke\"}}" localhost:3652/command
```

If you see something like this as the output...

```bash
{"scriptExecutionId":"se1"}
```

Then congrats! You started your first RockScript.

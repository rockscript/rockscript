## Prerequisites

 * A running RockScript server.  See <a onclick="show('getting-started')">Getting started</a> 
 * NodeJS 7.7.1+

## Your first script

```javascript
var http = system.import('rockscript.io/http');

var response = http.get({url:'http://api.icndb.com/jokes/random'});

var joke = response.body.value.joke;
```

`http.get(...);` in line 4 will perform a HTTP request and makes 
a HTTP response object available in the script.  For this particular URL,
the response object looks like this:

```
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

## Deploying the script

#### Deploy using the rock command

Type

```bash
rock deploy docs/examples/chuck/
```

You should get output like 

```bash
$  rock deploy docs/examples/chuck/
  Scanning directory /Code/rockscript/docs/examples/chuck (not recursive) for files matching .*\.rs(t)?
  Deploying docs/examples/chuck/get-joke.rs to http://localhost:3652 ...
  > POST http://localhost:3652/command
    Content-Type: application/json
    {
      "saveScript": {
        "scriptName": "docs/examples/chuck/get-joke.rs",
        "scriptText": "var http \u003d system.import(\u0027rockscript.io/http\u0027);\n\nvar response \u003d http.get({url:\...
        "activate": true
      }
    }
  < HTTP/1.1 200 OK
    Content-Type: application/json
    {
      "id": "sv1",
      "scriptId": "s1",
      "name": "docs/examples/chuck/get-joke.rs",
      "version": 1,
      "text": "var http \u003d system.import(\u0027rockscript.io/http\u0027);\n\nvar response \u003d http.get({url:\u0027htt...
      "active": true
    }
  1 scripts successful deployed
```

#### Deploy using only bash 

Paste this into the command line to save your first script version
 
```bash
$ curl -v -H "Content-Type: application/json" \
  -d "{\"saveScript\":{ \
    \"scriptName\": \"get-joke.rs\", \
    \"scriptText\": \"var http = system.import('rockscript.io\/http');\r\n\r\nvar response = http.get({url:'http:\/\/api.icndb.com\/jokes\/random'});\r\n\r\nvar joke = response.body.value.joke;\", \
    \"activate\": true \
  }}" \
  http://localhost:3652/command
```

You should see a response like this:

```bash
{"id":"sv2","scriptId":"s1","name":"get-joke.rs","version":1,"text":"var http..."}
```

### Starting the script 

```bash
curl -v -H "Content-Type:application/json" -d "{\"startScript\":{\"scriptName\":\"get-joke.rs\"}}" localhost:3652/command
```

If you see something like this as the output...

```bash
{"scriptExecutionId":"se1"}
```

Then congrats! You started your first RockScript.

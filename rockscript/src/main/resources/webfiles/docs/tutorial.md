### Prerequisites

 * A running RockScript server.  See <a onclick="show('getting-started')">Getting started</a> 
 * NodeJS 7.7.1+

## Your first script

This is the script we're going to execute.

```javascript
var http = system.import('rockscript.io/http');

var response = http.get({url:'http://api.icndb.com/jokes/random'});

var joke = response.body.value.joke;
```

The script is available at `docs/examples/chuck/get-joke.rs`

## The joke service 

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

## Deploy the script

When we deploy the script to the server, we 
* Create a script named `get-joke.rs`, if no script with that name exists yet
* Create a new script version with the given text
* Mark the new script version as the active script version.  The active script version is the version that is started 
  when a new execution is started.
  
For more about deployment and versioning, see <a onclick="show('script-versioning')">Script versioning</a>    

#### Deploy using the rock command

To deploy this script to your local running RockScript engine, Type

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
      "deployScript": {
        "scriptName": "docs/examples/chuck/get-joke.rs",
        "scriptText": "var http \u003d system.import(\u0027rockscript.io/http\u0027);\n\nvar response \u003d http.get({url:\...
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

If you want to see all the options for the `deploy` command, type

```bash
rock help deploy
```

#### Deploy using only bash 

Paste this into the command line to save your first script version
 
```bash
$ curl -v -H "Content-Type: application/json" \
  -d "{\"deployScript\":{ \
    \"scriptName\": \"get-joke.rs\", \
    \"scriptText\": \"var http = system.import('rockscript.io\/http');\r\n\r\nvar response = http.get({url:'http:\/\/api.icndb.com\/jokes\/random'});\r\n\r\nvar joke = response.body.value.joke;\" \
  }}" \
  http://localhost:3652/command
```

You should see a response like this:

```bash
{
  "id": "sv1",
  "scriptId": "s1",
  "name": "get-joke.rs",
  "version": 1,
  "text": "var http \u003d system.import(\u0027rockscript.io/http\u0027);\r\n\r\nvar response \u003d http.get({url:\u0027http://api.icndb.com/jokes/random\u0027});\r\n\r\nvar joke \u003d response.body.value.joke;",
  "active": true
}
```

## Starting the script

When starting an execution of the script, we'll identify the script by name. In 
this case, that's `get-joke.rs`   

#### Start script using rock

```bash
rock start -n get-joke.rs
```

If you want to see more options for the `start` command, type

```bash
rock help start
``` 

#### Start script using only bash

```bash
curl -v -H "Content-Type:application/json" \
     -d "{\"startScript\":{\"scriptName\":\"get-joke.rs\"}}" \
     localhost:3652/command
```

Then congrats! You started your first RockScript.

## Querying your script execution

Of course, now you want to see what the server has done.  Let's query 
the script execution details, including all the events that the server 
has stored.

```bash
curl http://localhost:3652/query?q=execution\&id=se1
```

Next, check out the <a onclick="language">language details</a> 
and become a RockScript wizard.
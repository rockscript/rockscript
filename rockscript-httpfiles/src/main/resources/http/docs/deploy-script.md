Creates a new script version on the server and activates it.  The active  
script version will be used when starting a new script execution for the script.

@see also <a onclick="show('script-versioning')">Script versioning</a>  

## Example
Here's a `saveScript` example
 
```
> POST /command
  Content-Type: application/json
  { "deployScript" :
    { "scriptName" : "Test script",
      "scriptText" : "var a\u003d0;"
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

## Request properties

| Property name | Required? | Type |Description |
|---|---|---|---|
| `scriptId` | Optional | String | ID that identifies the script for which this command creates a new version. |
| `scriptName` | Optional.  Default value is *Unnamed script* | String | Name that identifies the script for which this command creates a new version. If no `scriptId` nor `scriptName` is specified, *Unnamed script* is used as the scriptName. |
| `scriptText` | Required | String | The script text |

## Response properties

| Property name | Data type | Description |
|---|---|---|
| `id` | String | ID of the script version |
| `scriptId` | String | ID of the script |
| `scriptName` | String |Name of the script |
| `version` | Number |Sequential version number assigned by the server for this version, starts at 1 |
| `text` | String | Text of this script version |
| `active` | Boolean | true if this is the active version, absent if this is not the active version.  See <a onclick="show('script-versioning')">Script versioning</a>  |

## Failures

The request returns a `400 BAD REQUEST` in the following situations

* The script is identified by `scriptId` and that script is not found.

In each of these, a json body will be returned with an error message.  For example 

```
HTTP/1.1 400 BAD REQUEST
Content-Type: application/json
{ "message": "Script s928749 does not exist" }
```

## Save without activate

To create a new version without promoting that new version 
to the active version, use `saveScript` as the command name.

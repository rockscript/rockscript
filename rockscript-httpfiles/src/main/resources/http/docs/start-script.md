Starts a new script execution.  If you identify a script with properties 
`scriptId` or `scriptName`, then the latest version of that script will 
be used.  @see also <a onclick="show('script-versioning')">Script versioning</a>

## Example
Here's a `startScript` example
 
```
> POST /command
  Content-Type: application/json
  { "startScript" :
    { "scriptName" : "Submit order",
      "input" : {
        item: "Donuts",
        quantity: 7 
      }
    }
  }
< HTTP/1.1 200 OK
  Access-Control-Allow-Origin: *
  { "scriptExecutionId": "se1" }
```

## Request properties

| Property name | Required? | Type | Description |
|---|---|---|---|
| `scriptId` | One of `scriptId` or `scriptName` or `scriptVersionId` is required | String | ID that identifies the script for which the latest version should be started |
| `scriptName` | One of `scriptId` or `scriptName` or `scriptVersionId` is required | String | Name that identifies the script for which the latest version should be started |
| `scriptVersionId` | One of `scriptId` or `scriptName` or `scriptVersionId` is required | String | The ID of the exact script version that should be started |
| `input` | Optional | Any json type | The system.input value.  See also <a onclick="show('language')">Language</a>   |

## Response properties

| Property name | Data type | Description |
|---|---|---|
| `scriptExecutionId` | String | ID of the script execution |

## Failures

The request returns a `400 BAD REQUEST` in the following situations

* The script for the given `scriptId` or `scriptName` is not found
* The script version for the given `scriptVersionId` is not found
* No `scriptId`, `scriptName` or `scriptVersionId` are given
* The specified script does not have an active script version yet.  Fix this by using the `deployScript` instead of the `saveScript` command.

In each of these, a json body will be returned with an error message.  For example 

```
HTTP/1.1 400 BAD REQUEST
Content-Type: application/json
{ "message": "No script found with name UnexistingScript.rs" }
```
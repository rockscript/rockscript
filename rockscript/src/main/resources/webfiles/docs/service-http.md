## get

Performs a HTTP get request.

#### Input parameters

As input, this service function expects exactly 1 parameter which 
must be a JSON object.  The following properties can be specified:

| Property name | Required? | Description |
|---------------|-----------|-------------|
| `url` | Required | The full url including the protocol up to the query parameters |
| `headers` | Optional | A JSON object where the property names are the header names.  The value can be a string or an array of strings. (Note that array literals are not yet supported) |

A body is not supported in the `get` service function.

#### Return value

A response object looks like this
```
{ status: 200, 
  headers: {
    Date: "Thu, 30 Nov 2017 14:48:30 GMT", 
    Content-Type: "application/json" 
  }, 
  body: {
    some: "JSON"
  }
}
```

If the Content-Type is `application/json`, then the body is parsed as such and 
made available as an object so that it can be navigated. 

#### Errors

TODO

#### Example

```
var http = system.import('rockscript.io/http');

var servicePort = 9898;
var response = http.get({
  url: 'http://localhost:' + servicePort + '/?p1=v1&p2=v2',
  headers: {
    Header-One: 'singlevalue'
    Header-Two: ['array', 'value']
  }
});
```

## post
## `put`
## `delete`


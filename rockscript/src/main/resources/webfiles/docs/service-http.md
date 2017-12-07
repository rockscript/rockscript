The HTTP service provides functions get, post, put and delete.
The service is a built-in service and can be imported with 
url `rockscript.io/http`

## get

Performs a HTTP get request.

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

#### Failures

A request failes when no connection could be established 
or when the connection gets terminated prematurely.

*By default, an unexpected response status is not a failure.*  
If you want to non-expected response status messages to be considered 
as a failure, specify the property `expectedStatus` in the 
request like this
 
```
var http = system.import('rockscript.io/http');

var response = http.get({
  url: 'http://...',
  expectedStatus: 200
});
```

This request will now fail if the response status is not 200.

#### Retries

In case of a failure, the default is to retry 3 times 
with incremental backoff.  You can customize the retry policy.
Please ask for details how to do that 
[in a Github issue](https://github.com/rockscript/rockscript/issues/new?title=How+to+specify+incremental+backoff+in+the+http+service?).

## post

Same as for service function `get`, except for the `body` property.  
In a post, you can specify a body.

#### Example

```
var http = system.import('rockscript.io/http');

var response = http.post({
  url: 'http://localhost/orders',
  headers: {
    Content-Type: application/json
  },
  body: {
    item: 'Donut',
    amount: 6
  }
});
```

## put

Same as post (with body property)

## delete

Same as get (without body property)

var http = system.import('rockscript.io/http');

// This is a request to ask if you're lucky.
// The response will alternate between status 200 OK and
// status 500 INTERNAL SERVER ERROR with each request made
var response = http.get({
  url: 'http://localhost:3652/examples/lucky?',
  expectedStatus: 200
});

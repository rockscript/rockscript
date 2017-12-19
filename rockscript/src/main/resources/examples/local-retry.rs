var http = system.import('rockscript.io/http');

// This request will fail every other attempt.
// The response will alternate between
// status 200 OK and status 500 INTERNAL
// SERVER ERROR with each request
var response = http.get({
  url: 'http://localhost:3652/examples/lucky?',
  expectedStatus: 200
});

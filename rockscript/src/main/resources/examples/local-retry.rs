var http = system.import('rockscript.io/http');

// This will succeed after a retry
// By specifying the expectedStatus, the request will fail if the
// response status is different from 200.
var response = http.get({
  url: 'http://localhost:3652/examples/succeed-every-one-in-two',
  expectedStatus: 200
});

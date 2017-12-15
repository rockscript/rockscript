var http = system.import('rockscript.io/http');

// This will wait indefinite as the next function call will never succeed
var response = http.get({
  url: 'http://unexisting-host'
});

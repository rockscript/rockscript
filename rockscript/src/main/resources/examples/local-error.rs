var http = system.import('rockscript.io/http');

// This will wait indefinite as the next
// get invocation will never succeed
var response = http.get({
  url: 'http://unexisting-host'
});

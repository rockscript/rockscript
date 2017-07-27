// Demonstrates HTTP response logging in the server console.

var http = system.import('rockscript.io/http');

http.request({
  url: 'http://wttr.in/RTM?no-terminal=0',
  headers: {
    'User-Agent': 'curly'
  }
});

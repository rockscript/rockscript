var http = system.import('rockscript.io/http');
var jokes = system.import('localhost:3000');

var chuckResponse = http.get({url:'http://api.icndb.com/jokes/random'});

jokes.joke(chuckResponse.body.value.joke);
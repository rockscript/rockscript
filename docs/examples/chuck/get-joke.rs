var http = system.import('rockscript.io/http');

var response = http.get({url:'http://api.icndb.com/jokes/random'});

var joke = response.body.value.joke;
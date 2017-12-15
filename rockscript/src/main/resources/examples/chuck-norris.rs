var http = system.import('rockscript.io/http');

// Gets a random Chuck Norris joke
var chuckResponse = http.get({url: 'http://api.icndb.com/jokes/random'});

// Get the joke from the response body
var jokeEncoded = encodeURI(chuckResponse.body.value.joke);

// Transform the joke to a nice text representation
var textResponse = http.get({
  url: 'http://cowsay.morecode.org/say?message='+jokeEncoded+'&format=text'
});

// Check out the expanded value of text
var text = textResponse.body;

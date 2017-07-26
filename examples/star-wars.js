// Fetches the name of Luke Skywalker’s homeworld from the Star Wars API
// and sends it to a RequestBin.

var http = system.import('rockscript.io/http');

// Note: spoof the user agent to HTTPie so Cloudflare doesn’t complain.
var luke = http.request({
  url: 'http://swapi.co/api/people/1/',
  headers: {
    'Accept': 'application/json',
    'User-Agent': 'HTTPie/0.8.0'
  }
});

var planet = http.request({
  url: luke.json.homeworld,
  headers: {
    'Accept': 'application/json',
    'User-Agent': 'HTTPie/0.8.0'
  }
});

// Create a new https://requestb.in/ and update the URL.
http.request({
  method: 'POST',
  url: 'https://requestb.in/tkr719tk',
  headers: {
    'Content-Type': 'application/json'
  },
  body: {
    destination: planet.json.name
  }
});

var http = system.import('rockscript.io/http');

// Get Luke Skywalker data
var lukeResponse = http.get({
  url: 'https://swapi.co/api/people/1/'
});

// Luke Skywalker's first vehicle
var homeworldUrl = lukeResponse.body.homeworld;

// Get all the data from Like's first vehicle
var homeworld = http.get({url: homeworldUrl});

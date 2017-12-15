var http = system.import('rockscript.io/http');

// Get Luke Skywalker data
var lukeResponse = http.get({url: 'https://swapi.co/api/people/1/'});

// Luke Skywalker's first vehicle
var firstVehicleUrl = lukeResponse.vehicles[0];

// Get all the data from Like's first vehicle
var firstVehicle = http.get({url: firstVehicleUrl});

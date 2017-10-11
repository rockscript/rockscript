var http = system.import('rockscript.io/http');

var stations = http.get({
  url: 'https://'+system.input.host+'/stations/NMBS?q=Turnhout',
  headers: {
    Accept: 'application/json'
  }
});

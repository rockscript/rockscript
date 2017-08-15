var http = system.import('rockscript.io/http');
var requestbin = system.import('rockscript.io/requestbin');

var rates = http.get({url:'http://api.fixer.io/latest'});
requestbin.postToBin({ message: '1 EUR is ... USD' });
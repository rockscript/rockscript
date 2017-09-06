var http = system.import('rockscript.io/http');
var approvalService = system.import('localhost:3000');

var chuckResponse = http.get({url:'http://api.icndb.com/jokes/random'});

approvalService.approve(chuckResp onse.body.value.joke);
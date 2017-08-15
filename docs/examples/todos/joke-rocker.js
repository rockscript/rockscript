const express = require('express');
const http = require("http");
const bodyParser = require('body-parser');
const app = express();

app.use(bodyParser.json());

app.post('/addJoke', function (req, res) {
  postJokeToServer(req.body.args[0], function() {
    res.send({ ended: false });
  });
});

function postJokeToServer(title, success) {
  var postRequest = http.request({
    host: 'localhost',
    port: 4000,
    path: '/jokes',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    function(postResponse) {
      postResponse.on('end', success);
    }
  });
  postRequest.write(JSON.stringify({title: title}));
  postRequest.end();
}

app.listen(3000, function () {
  console.log('Joke Rocker listening on port 3000!');
});

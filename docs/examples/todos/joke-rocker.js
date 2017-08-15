const express = require('express');
const http = require("http");
const bodyParser = require('body-parser');
const app = express();

app.use(bodyParser.json());

app.post('/addJoke', function (req, res) {
  postJokeToServer(req.body.args[0], res);
});

function postJokeToServer(title, res) {
  var postRequest = http.request({
      method: 'POST',
      hostname: 'localhost',
      port: 4000,
      path: '/jokes',
      headers: {
        'Content-Type': 'application/json'
      }
    },
    (res) => {
      console.log(`STATUS: ${res.statusCode}`);
      console.log(`HEADERS: ${JSON.stringify(res.headers)}`);
      res.setEncoding('utf8');
      res.on('data', (chunk) => {});
      res.on('end', function() {
        console.log('Post success');
        res.send({ ended: false });
      });
    }
  );
  postRequest.write(JSON.stringify({title: title}));
  postRequest.end();
}

app.listen(3000, function () {
  console.log('Joke Rocker listening on port 3000!');
});

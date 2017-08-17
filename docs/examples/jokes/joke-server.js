const express = require('express');
const bodyParser = require('body-parser');
const http = require("http");
const app = express();
const shortid = require('shortid');

app.use(express.static('public'));
app.use(bodyParser.json());

var jokes = [];

app.get('/jokes', function (req, res) {
  res.send(jokes);
});

app.post('/joke', function (req, res) {
  var actionInput = req.body;
  console.log('New joke started with actionInput: '+JSON.stringify(actionInput, null, 2));
  var joke = {
    id: shortid.generate(),
    title: actionInput.args[0],
    actionPosition: {
      scriptExecutionId: actionInput.scriptExecutionId,
      executionId: actionInput.executionId
    }
  }
  jokes.push(joke);
  res.send({ ended: false });
});

app.delete('/jokes/:jokeId', function (req, res) {
  var deleteJokeId = req.params.jokeId;
  var joke = jokes.find((joke,i,jokes)=>{
    if (joke.id === deleteJokeId) {
      jokes.splice(i, 1);
      return true;
    }
    return false;
  });
  res.sendStatus(200);

  var endActionCommand = {
    endAction: {
      scriptExecutionId: joke.actionPosition.scriptExecutionId,
      executionId: joke.actionPosition.executionId
    }
  };

  console.log('Ending joke action by posting the end action command:');
  console.log('  POST http://loclahost:8888/endAction');
  console.log('  Content-Type: application/json');
  console.log('  '+JSON.stringify(endActionCommand, null, 2));

  var endActionRequest = http.request({
      method: 'POST',
      hostname: 'localhost',
      port: 8888,
      path: '/command',
      headers: {
        'Content-Type': 'application/json'
      }
    },
    (res) => {
      res.on('data', (chunk) => {});
      res.on('end', function() {
        console.log('RockScript server was notified of joke end: '+deleteJokeId);
      });
    }
  );
  endActionRequest.on('error', (e) => {
    console.error(`Got error: ${e.message}`);
  });
  endActionRequest.write(JSON.stringify(endActionCommand));
  endActionRequest.end();

});

app.get('/', function (req, res) {
  res.sendFile(__dirname+'/public/index.html');
});

app.listen(3000, function () {
  console.log('Joke Server listening on port 3000!');
});

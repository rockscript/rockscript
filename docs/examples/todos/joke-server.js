const express = require('express');
const bodyParser = require('body-parser');
const app = express();
const shortid = require('shortid');

app.use(express.static('public'));
app.use(bodyParser.json());

var jokes = [];

app.get('/jokes', function (req, res) {
  res.send(jokes);
});

app.post('/jokes', function (req, res) {
  var joke = req.body;
  joke.id = shortid.generate();
  jokes.push(joke);
  res.send(joke);
});

app.delete('/jokes/:jokeId', function (req, res) {
  jokes = jokes.filter(joke=>joke.id != req.params.jokeId);
  res.sendStatus(200);
});

app.get('/', function (req, res) {
  res.sendFile(__dirname+'/public/index.html');
});

app.listen(4000, function () {
  console.log('Joke Server listening on port 4000!');
});

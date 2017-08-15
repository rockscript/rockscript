const express = require('express');
const bodyParser = require('body-parser');
const app = express();

app.use(express.static('public'));
app.use(bodyParser.json());

var todos = [{id:0, title:'Dishes'}, {id:1, title:'Laundry'}];

app.get('/todos', function (req, res) {
  res.send(todos);
});

app.post('/todos', function (req, res) {
  var todo = req.body;
  todo.id = todos.length;
  todos.push(todo);
  res.send(todo);
});

app.delete('/todos/:todoId', function (req, res) {
  console.log('before: '+JSON.stringify(todos)+' : '+req.params.todoId);
  todos = todos.filter(todo=>todo.id != req.params.todoId);
  console.log('after: '+JSON.stringify(todos));
  res.sendStatus(200);
});

app.get('/', function (req, res) {
  res.sendFile(__dirname+'/public/index.html');
});

app.listen(3000, function () {
  console.log('App listening on port 3000!');
});

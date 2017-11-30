const express = require('express');
const bodyParser = require('body-parser');
const http = require("http");
const shortid = require('shortid');

const app = express();
app.use(bodyParser.json());

var approvals = [];

app.get('/approvals', function (req, res) {
  res.send(approvals);
});

app.post('/approve', function (req, res) {
  var input = req.body;
  var approval = {
    id: shortid.generate(),
    title: input.args[0],
    continuation: {
      scriptExecutionId: input.scriptExecutionId,
      executionId: input.executionId
    }
  }
  approvals.push(approval);

  console.log('New approval created with incoming request: ');
  console.log('  POST http://localhost:3000/approval');
  console.log('  Content-Type: application/json');
  console.log('  '+JSON.stringify(input, null, 2));

  res.send({ ended: false });
});

app.delete('/approvals/:approvalId', function (req, res) {
  var deleteApprovalId = req.params.approvalId;
  var approval = approvals.find((approval,i,approvals)=>{
    if (approval.id === deleteApprovalId) {
      approvals.splice(i, 1);
      return true;
    }
    return false;
  });
  res.sendStatus(200);

  var endFunctionCommand = {
    endActivity: {
      scriptExecutionId: approval.continuation.scriptExecutionId,
      executionId: approval.continuation.executionId
    }
  };

  console.log('Approval received and notifying rockscript server with outgoing request: ');
  console.log('  POST http://localhost:3652/command');
  console.log('  Content-Type: application/json');
  console.log('  '+JSON.stringify(endFunctionCommand, null, 2));

  var endFunctionRequest = http.request({
      method: 'POST',
      hostname: 'localhost',
      port: 3652,
      path: '/command',
      headers: {
        'Content-Type': 'application/json'
      }
    },
    (res) => {
      res.on('data', (chunk) => {});
      res.on('end', function() {
        console.log('RockScript server was notified of approval: '+deleteApprovalId);
      });
    }
  );
  endFunctionRequest.on('error', (e) => {
    console.error(`Got error: ${e.message}`);
  });
  endFunctionRequest.write(JSON.stringify(endFunctionCommand));
  endFunctionRequest.end();

});

app.get('/', function (req, res) {
 res.sendFile(__dirname+'/public/index.html');
});

app.listen(3000, function () {
  console.log('Approval service listening on port 3000.  Point your browser to http://localhost:3000');
});

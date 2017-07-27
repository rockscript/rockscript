// Registers a user with email address confirmation.
// Required features: SMTP action, timeout, script HTTP callback, script end action

var smtp = system.import('rockscript.io/http');
var template = system.import('rockscript.io/template');

smtp.send({
  to: script.input.emailAddress,
  subject: 'Confirm registration',
  body: template.render(script.input.template, script.url)
});

var timer = system.import('rockscript.io/timer');

timer.timeout({
  duration: "24h",
  action: script.end
});

script.waitForCallbackUrlRequest();

var http = system.import('rockscript.io/http');

http.post({
  url: 'http://api.example.com/users',
  body: {
    name: script.input.name,
    email: script.input.emailAddress
  }
})

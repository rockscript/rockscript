var test = system.import('rockscript.io/test');

test.start({
  scriptName: '../docs/examples/test/list-trains.rs',
  input: {
    host: 'irail.be'
  }
});

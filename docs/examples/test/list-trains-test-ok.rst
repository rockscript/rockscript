var test = system.import('rockscript.io/test');

test.start({
  script: '.*/list-trains.rs',
  input: {
    host: 'irail.be'
  }
});

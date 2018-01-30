subtract(1, 2);
subtract(1, 'y');
subtract(1, true);
subtract(1, null);
subtract(1, undefined);
subtract(1, {'a':1});
subtract(1, ['a',1]);
console.log();

subtract('x', 2);
subtract('x', 'y');
subtract('x', true);
subtract('x', null);
subtract('x', undefined);
subtract('x', {'a':1});
subtract('x', ['a',1]);
console.log();

subtract(true, 2);
subtract(true, 'y');
subtract(true, true);
subtract(true, null);
subtract(true, undefined);
subtract(true, {'a':1});
subtract(true, ['a',1]);
console.log();

subtract(null, 2);
subtract(null, 'y');
subtract(null, true);
subtract(null, null);
subtract(null, undefined);
subtract(null, {'a':1});
subtract(null, ['a',1]);
console.log();

subtract(undefined, 2);
subtract(undefined, 'y');
subtract(undefined, true);
subtract(undefined, null);
subtract(undefined, undefined);
subtract(undefined, {'a':1});
subtract(undefined, ['a',1]);
console.log();

subtract({'a':1}, 2);
subtract({'a':1}, 'y');
subtract({'a':1}, true);
subtract({'a':1}, null);
subtract({'a':1}, undefined);
subtract({'a':1}, {'a':1});
subtract({'a':1}, ['a',1]);
console.log();

subtract(['a',1], 2);
subtract(['a',1], 'y');
subtract(['a',1], true);
subtract(['a',1], null);
subtract(['a',1], undefined);
subtract(['a',1], {'a':1});
subtract(['a',1], ['a',1]);

function subtract(l,r) {
  var result = l-r;
  console.log('    assertSubtract("'+result+'", "'+valueToString(l)+'-'+valueToString(r)+'");');
}

function valueToString(o) {
  return ('string'===typeof o? '\''+o+'\'' : 'object'===typeof o || 'array'===typeof o ? JSON.stringify(o).replace('"', "'") :o );
}
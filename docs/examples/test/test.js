divide(1, 2);
divide(1, 'y');
divide(1, true);
divide(1, null);
divide(1, undefined);
divide(1, {'a':1});
divide(1, ['a',1]);
console.log();

divide('x', 2);
divide('x', 'y');
divide('x', true);
divide('x', null);
divide('x', undefined);
divide('x', {'a':1});
divide('x', ['a',1]);
console.log();

divide(true, 2);
divide(true, 'y');
divide(true, true);
divide(true, null);
divide(true, undefined);
divide(true, {'a':1});
divide(true, ['a',1]);
console.log();

divide(null, 2);
divide(null, 'y');
divide(null, true);
divide(null, null);
divide(null, undefined);
divide(null, {'a':1});
divide(null, ['a',1]);
console.log();

divide(undefined, 2);
divide(undefined, 'y');
divide(undefined, true);
divide(undefined, null);
divide(undefined, undefined);
divide(undefined, {'a':1});
divide(undefined, ['a',1]);
console.log();

divide({'a':1}, 2);
divide({'a':1}, 'y');
divide({'a':1}, true);
divide({'a':1}, null);
divide({'a':1}, undefined);
divide({'a':1}, {'a':1});
divide({'a':1}, ['a',1]);
console.log();

divide(['a',1], 2);
divide(['a',1], 'y');
divide(['a',1], true);
divide(['a',1], null);
divide(['a',1], undefined);
divide(['a',1], {'a':1});
divide(['a',1], ['a',1]);

function divide(l,r) {
  var result = l/r;
  console.log('    assertDivide('+valueToString(result)+', "'+valueToString(l)+'/'+valueToString(r)+'");');
}

function valueToString(o) {
  return ('string'===typeof o? '\''+o+'\'' : 'object'===typeof o || 'array'===typeof o ? JSON.stringify(o).replace(new RegExp('"', 'g'), "'") :o );
}
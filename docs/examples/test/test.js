compare(1, 2);
compare(1, 'y');
compare(1, true);
compare(1, null);
compare(1, undefined);
compare(1, {'a':1});
compare(1, ['a',1]);
console.log();

compare('x', 2);
compare('x', 'y');
compare('x', true);
compare('x', null);
compare('x', undefined);
compare('x', {'a':1});
compare('x', ['a',1]);
console.log();

compare(true, 2);
compare(true, 'y');
compare(true, true);
compare(true, null);
compare(true, undefined);
compare(true, {'a':1});
compare(true, ['a',1]);
console.log();

compare(null, 2);
compare(null, 'y');
compare(null, true);
compare(null, null);
compare(null, undefined);
compare(null, {'a':1});
compare(null, ['a',1]);
console.log();

compare(undefined, 2);
compare(undefined, 'y');
compare(undefined, true);
compare(undefined, null);
compare(undefined, undefined);
compare(undefined, {'a':1});
compare(undefined, ['a',1]);
console.log();

compare({'a':1}, 2);
compare({'a':1}, 'y');
compare({'a':1}, true);
compare({'a':1}, null);
compare({'a':1}, undefined);
compare({'a':1}, {'a':1});
compare({'a':1}, ['a',1]);
console.log();

compare(['a',1], 2);
compare(['a',1], 'y');
compare(['a',1], true);
compare(['a',1], null);
compare(['a',1], undefined);
compare(['a',1], {'a':1});
compare(['a',1], ['a',1]);
console.log();

function compare(l,r) {
  console.log('    assertCompare('+(l!=r)+', "'+valueToString(l)+'!='+valueToString(r)+'");');
  console.log('    assertCompare('+(r!=l)+', "'+valueToString(r)+'!='+valueToString(l)+'");');
}

function valueToString(o) {
  return ('string'===typeof o? '\''+o+'\'' : 'object'===typeof o || 'array'===typeof o ? JSON.stringify(o).replace(new RegExp('"', 'g'), "'") :o );
}
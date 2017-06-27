function test(cb) {
	cb();
}

function foo(x) { 
	console.log(x);
}

function bar(x) { 
	console.log(x);
}

test(foo);
test(bar);

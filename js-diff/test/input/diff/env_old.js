function test(cb) {
	cb();
}

function foo(x) { 
	console.log(x.greeting);
}

function bar(x) { 
	console.log(x.greeting);
}

test(foo);
test(bar);

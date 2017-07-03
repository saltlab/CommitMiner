function test(cb) {
	cb();
}

function foo(x) { 
	console.log("Foo says: " + x.greeting);
}

function bar(x) { 
	console.log("Bar says: " + x.greeting);
}

test(foo);
test(bar);

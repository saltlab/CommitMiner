var message = { greeting: "Hello World!" };

function test(cb) {
	cb(message);
}

function foo(x) { 
	console.log(x.greeting);
}

function bar(x) { 
	console.log(x.greeting);
}

test(foo);
test(bar);

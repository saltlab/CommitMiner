var message = { greeting: "Hello World!" };

function test(cb) {
	cb(message);
}

function foo(x) { 
	if(!x) return;
	console.log(x.greeting);
}

function bar(x) { 
	if(!x) return;
	console.log(x.greeting);
}

test(foo);
test(bar);

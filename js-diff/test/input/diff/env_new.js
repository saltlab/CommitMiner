var message = "Hello World!";

function test(cb) {
	cb(message);
}

function foo(x) { 
	console.log(x);
}

function bar(x) { 
	console.log(x);
}

test(foo);
test(bar); // TODO: Why doesn't the analysis re-analyze test here? Do we check for changes to parameters, or only store?

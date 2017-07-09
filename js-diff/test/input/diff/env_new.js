var message = { greeting: "Hello World!" };

function test(cb) {
	cb(message);
}

function foo(x) { 
	if(!x) return;
	console.log("Foo says: " + x.greeting); // TODO: Why does x.greeting resolve to two values when we detect globals?
}

function bar(x) { 
	if(!x) return;
	console.log("Bar says: " + x.greeting);
}

test(foo);
test(bar);

var config = require('config');
console.log(config.setting);

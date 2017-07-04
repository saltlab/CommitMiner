var message = { greeting: "Hello World!" };

function test(cb) {
	cb(message);
}

function foo(x) { 
	if(!x) return;
	console.log("Foo says: " + x.greeting);
}

function bar(x) { 
	if(!x) return;
	console.log("Bar says: " + x.greeting);
}

test(foo);
test(bar);

var config = { setting: true };
console.log(config.setting);

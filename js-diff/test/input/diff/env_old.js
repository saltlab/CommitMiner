function test(cb) {
	cb();
}

function foo(x) { }

function bar(x) { }

test(foo);
test(bar); // TODO: Why doesn't the analysis re-analyze test here?

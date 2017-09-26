function test(cb) {
	return cb ? cb({success:false}) : null;
}

function foo(data) {
	console.log(data.success);
}

test(foo);

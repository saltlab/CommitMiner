function test(cb) {
	return cb ? cb({success:true}) : null;
}

function foo(data) {
	console.log(data.success);
}

test(foo);

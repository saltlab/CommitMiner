function test(cb) {
	cb({success:true});
}

function foo(data) {
	console.log(data.success);
}

test(foo);

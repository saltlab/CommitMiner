var y;

function A(cb) {
	cb();
	console.log(y);
}


function B() {

	var x = "Hello World!";

	function C() {
		console.log(x);
		y = 0;
	}

	A(C);
	
}

B();

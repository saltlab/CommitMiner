
var a = { l: 5, r: 10 };

function A(x) {
	var z = 10; 
	console.log((x.l*z + x.r*z));
}

A(a);
a.r = 5;
A(a);


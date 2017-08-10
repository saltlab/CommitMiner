var config = require('config');
var fs = require('fs');
var message1 = { greeting: "Welcome!" };

function test(cb) {
	cb();
}

function foo(x) { 
	console.log("Foo says: " + x.greeting);
}

function bar(x) { 
	console.log("Bar says: " + x.greeting);
}

function gaf(x) {
	console.log("Gaf says: " + x.greeting);
}

function aru(err, x) {
	if(!x) return;
	console.log("Aru says: " + x.greeting);
}

test(foo);

function bli(x) {
	console.log("Bli says: " + x.greeting);
}

function cam(x) {

	function erp(y) {
		console.log("Cam says: " + x.greeting);
		console.log("Erp says: " + y.greeting);
	}

	function dal(y) {
		console.log("Cam says: " + y.greeting);
		console.log("Dal says: " + y.greeting);
	}

	erp(message1);
	dal(message1);	

}

function drp(err, x) {
	if(!x) return;
	console.log("Drp says: " + x.greeting);
}

console.log("Beginning test sequence 1.");

gaf(message1);

bar(message1);

aru(err, message1);

bli(message1);

cam(message2);

drp(err, message2);

console.log("Beginning test sequence 2.");

test(bar);


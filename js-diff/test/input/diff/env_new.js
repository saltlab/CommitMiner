var config = require('config');
var fs = require('fs');
var message1 = { greeting: "Welcome!" };
var message2 = { greeting: "Hello!" };

function test(cb) {
	cb(message2);
}

function foo(x) { 
	if(!x) return;
	console.log("Foo says: " + x.greeting);
}

function gaf(x) {
	console.log("Gaf says: " + x.greeting);
}

function bar(x) { 
	if(!x) return;
	console.log("Bar says: " + x.greeting);
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
		if(!x || y) return;
		console.log("Cam says: " + x.greeting);
		console.log("Erp says: " + y.greeting);
	}

	function dal(x) {
		if(!x || !y) return;
		console.log("Cam says: " + x.greeting);
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

bli(err, message1);

cam(message2);

drp(err, message2);

console.log("Beginning test sequence 2.");

test(bar);


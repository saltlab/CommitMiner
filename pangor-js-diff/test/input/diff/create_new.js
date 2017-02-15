var a = require('foo');
var b = 10;
var c = b;
var d = bar();
var e = bar();
var f = module;
var g = module.exports;
var h = call({ v: 0, w: 1});
var i = module.exports;
var j = "Hello World";
var k = { x: 5, y: "6",
	z:	function(one, two) {
				foo();	
				console.log(j);
			}
	};

function bar() {
	if(a) {
		return 10; }
	else if(f) {
		return 0; }
}

function gat() {
	return 5;
}

function foo() {
	return j;
}

function hip() {
	return function isp(cb) {
		var o = "No one calls this!";
		var p = bar();
		console.log(p);
		var q = f;
		console.log(q);
		cb();
	}
}

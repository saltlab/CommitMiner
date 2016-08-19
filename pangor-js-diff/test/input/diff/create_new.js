var a = require('foo');
var b = 10;
var c = b;
var d = bar();
var e = gat();
var f = module;
var g = module.exports;
var h = module;
var i = module.exports;

function bar() {
	if(a)
		return 10;
	else 
		return 0;
}

function gat() {
	return 5;
}

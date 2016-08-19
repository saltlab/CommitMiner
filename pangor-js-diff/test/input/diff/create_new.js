var a = require('foo'); // VAR = TOP (require could return a new value, although we can't see)
var b = 10;							// VAR = CHANGED (it is a new value)
var c = b;							// VAR = CHANGED (it is a new value)
var d = bar();					// VAR = TOP (bar could return a new value)
var e = gat();					// VAR = TOP (gat returns a new integer)
var f = module;					// VAR = UNCHANGED (we assume module is unchanged)
var g = module.exports; // VAR = UNCHANGED (we assume module.exports is unchanged)
var h = module;					// VAR = UNCHANGED (we assume module is unchanged)
var i = module.exports; // VAR = UNCHANGED (we assume module.exports is unchanged)

function bar() {
	if(a) {
		return 10; }
	else {
		return 0; }
}

function gat() {
	return 5;
}

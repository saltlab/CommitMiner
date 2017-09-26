function gat() {
	console.log("Hi Foo and Bar!");
}

function bar() {
	console.log("Hi Gat!");
	gat();
}

function foo() {
	console.log("Hi Bar!");
	bar();
}

foo();

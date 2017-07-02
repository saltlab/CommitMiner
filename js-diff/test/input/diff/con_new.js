function gat() {
	console.log("Hi Foo and Bar!");
}

function bar() {
	console.log("Hi Gat!");
	gat();
}

function foo() {
	if(!bar) return;
	console.log("Hi Bar!");
	bar();
}

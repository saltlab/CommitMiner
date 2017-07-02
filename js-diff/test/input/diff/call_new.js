
function bar() {
	console.log("Hello World!");
}

function foo() {
	console.log("Running bar");
	bar();
	console.log("bar complete");
}

foo();

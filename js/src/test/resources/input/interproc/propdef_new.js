function A(x) {
	if(x) {
		console.log(x.greeting + " " + x.name + "!");
	}
}

var params = {}

params.greeting = "Hello";
params.name = "Jax";

A(params);

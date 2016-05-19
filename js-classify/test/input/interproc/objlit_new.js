function A(x) {
	if(x) {
		console.log(x.greeting + " " + x.name + "!");
	}
}

var params = {
  greeting: "Hello",
	name: "Jax"
};

A(params);

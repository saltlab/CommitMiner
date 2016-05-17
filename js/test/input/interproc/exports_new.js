var punctuation = "!";
exports.greet = function(name, greeting) {
		if(name && greeting) {
			console.log(greeting + " " + name + punctuation);
		}
	}

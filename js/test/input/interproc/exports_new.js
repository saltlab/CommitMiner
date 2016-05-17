exports.greet = function(name, greeting) {
		var punctuation = "!";
		if(name && greeting) {
			console.log(greeting + " " + name + punctuation);
		}
	}

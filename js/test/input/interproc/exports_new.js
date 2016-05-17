var punctuation = "!";
var Common = module.exports;
Common.greet = function(name, greeting) {
		if(name && greeting) {
			console.log(greeting + " " + name + punctuation);
		}
	}

var punctuation = "!";
var Common = module.exports;
Common.greet = function(name, greeting) {
		var space = " ";
		if(name && greeting) {
			console.log(greeting + space + name + punctuation);
		}
		console.log("Thanks for visiting!");
	}

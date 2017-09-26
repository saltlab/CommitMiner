var reader = require('filereader') ;
var file = 'name.csv';
var name;
var message = {
	greeting: "Hello",
	print: function (punctuation) {
		console.log(this.greeting + " " + name + punctuation);	
	}
};

function setName(newName) {
	if(newName) {
		name = newName;	
	}
	else if(reader && file) {
		name = file.read(data);
	}
}

module.exports.greet = function(punctuation) {
	message.print(punctuation);	
}

module.exports.setPerson = function(newName) {
	setName(newName);
}

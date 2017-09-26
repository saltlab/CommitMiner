var reader = require('read-file') ;
var file = 'name.csv';
var defaultName = "Charles";
var name;
var message = {
	greeting: "Hello",
	print: function (punctuation) {
		console.info(this.greeting + " " + name + punctuation);	
	}
};

function setName(newName) {
	if(newName) {
		name = newName;	
	}
	else if(reader && file) {
		name = reader.read(data);
	}
	else if (defaultName){
		name = defaultName;
	}
}

setName("Robert");

module.exports.greet = function(punctuation) {
	message.print(punctuation);	
}

module.exports.setPerson = function(newName) {
	setName(newName);
}

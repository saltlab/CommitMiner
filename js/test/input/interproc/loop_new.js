var name = 5;
var greeting = "Hello";
var cnt = process.argv[2];

for(var i = 0; i < cnt; i++) {
	console.log(greeting + " " + name + "!");
}

if(cnt > 9) {
	console.log("That was a lot of greetings!");
}
else {
	console.log("That was a good greeting!");
}

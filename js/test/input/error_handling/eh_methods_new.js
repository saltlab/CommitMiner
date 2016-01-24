/* Uncaught exception
 * Output: EH_UNCAUGHT_EXCEPTION */ 

function throwsException() {
	throw new Error();
}

function doSomething() {
	try {
		throwsException();
	}
	catch (e) { }
}

function doSomethingElse() {
	try {
		throwsException();
	}
	catch (e) { }
}

doSomething();
doSomethingElse();

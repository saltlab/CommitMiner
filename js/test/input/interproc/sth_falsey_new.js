/* Special Type Handling: Falsey
 * Output: STH_TYPE_ERROR_FALSEY (a)
 *         STH_TYPE_ERROR_FALSEY (b)
 *         STH_TYPE_ERROR_FALSEY (c) */
var a = "Hello World!";
function A() {
	if(a) {
		console.log(a);
	}
}
A();

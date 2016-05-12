/* Special Type Handling: Falsey
 * Output: STH_TYPE_ERROR_FALSEY (a)
 *         STH_TYPE_ERROR_FALSEY (b)
 *         STH_TYPE_ERROR_FALSEY (c) */
var a = "Hello";
var b = "Jax";
function A(x) {
	if(a) {
		console.log(a + " " + x + "!");
	}
}
A(b);

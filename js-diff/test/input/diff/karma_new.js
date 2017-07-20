var createErrorFormatter = function () {

  return function () {

    if (true) { } // each of these adds 6 runs
    else if (true) { }
    else if (true) { }

    var msg = replace(function () {

      if (true) { } // each of these adds 12 runs
			else if(true) { }

      findFile();

    });

  }
}

var createReporters = function () {
  var errorFormatter = createErrorFormatter()

  foo(function (name) {
      foo(errorFormatter) // DEBUG accounts for 8 runs
      foo(errorFormatter) // DEBUG accounts for 8 runs
      foo(errorFormatter) // DEBUG accounts for 8 runs
  });

}


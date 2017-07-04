/*
 * MultiDiff
 */

$('.DEF-tag').click(function (event) {

	/* Remove highlighting from all defs/uses. */
	$('.DEF-tag').removeClass('DEF');
	$('.USE-tag').removeClass('USE');
	$('.USE-tag').removeClass('DEF');

	/* Highlight the definition and uses of this function. */
	var address = $(this).attr('data-address');
	$(this).addClass('DEF');
	$(".USE-tag[data-address='" + address + "']").addClass('USE');

});

$('.USE-tag').click(function (event) {

	/* Remove highlighting from all defs/uses. */
	$('.DEF-tag').removeClass('DEF');
	$('.USE-tag').removeClass('USE');
	$('.USE-tag').removeClass('DEF');

	/* Highlight the definition and uses of this function. */
	var address = $(this).attr('data-address');
	$(".USE-tag[data-address='" + address + "']").addClass('USE');
	$(".DEF-tag[data-address='" + address + "']").addClass('DEF');
	$(this).removeClass('USE');
	$(this).addClass('DEF');

});

/**
 * Remove all def-use highlighting.
 */
function erase() {
	$('.ENV-DEF').removeClass('def');
	$('.ENV-USE').removeClass('use');
	$('.VAL-DEF').removeClass('def');
	$('.VAL-USE').removeClass('use');
	$('.CALL-DEF').removeClass('def');
	$('.CALL-USE').removeClass('use');
	$('.CON-DEF').removeClass('def');
	$('.CON-USE').removeClass('use');
}

/**
 * Highlights all def/use spans.
 */
function all(def, use) {
	erase();
	$('.' + def).addClass('def');
	$('.' + use).addClass('use');
}

/**
 * Highlights the selected def/use spans.
 */
function sel(e, def, use) {

	erase();

	/* Find the span with the ENV-* tag. */
	var ids = null;
	var current = $(e.target);
	while(ids === null && current.prop("nodeName") === "SPAN") {

		var annotations = current.attr('class').split(' ');
		for(var i = 0; i < annotations.length; i++) {
			switch(annotations[i]) {
				case def:
				case use:
					ids = current.attr('data-address').split(',');
					break;
			}
		}

		current = current.parent();
	}

	if(ids === null) return;

	$("span." + use).each(function(index) {
		for(var i = 0; i < ids.length; i++) {
			if($(this).attr('data-address').split(',').indexOf(ids[i]) >= 0) {
				$(this).addClass('use');
			}
		}
	});

	$("span." + def).each(function(index) {
		for(var i = 0; i < ids.length; i++) {
			if($(this).attr('data-address').split(',').indexOf(ids[i]) >= 0) {
				$(this).addClass('def');
			}
		}
	});

}


/**
 * Highlight all ENV-DEF and ENV-USE spans.
 */
function allVar() {
	all('ENV-DEF', 'ENV-USE');
}

/**
 * Highlight the selected ENV-DEF and ENV-USE spans.
 */
function selVar(e) {
	sel(e, "ENV-DEF", "ENV-USE");
}

/**
 * Highlight all VAL-DEF and VAL-USE spans.
 */
function allVal() {
	all('VAL-DEF', 'VAL-USE');
}

/**
 * Highlight all CALL-DEF and CALL-USE spans.
 */
function allCall() {
	all('CALL-DEF', 'CALL-USE');
}

/**
 * Highlight all CON-DEF and CON-USE spans.
 */
function allCon() {
	all('CON-DEF', 'CON-USE');
}

$(function() {
	$.contextMenu({
		selector: '.context-menu', 
		build: function($trigger, e) {
			// this callback is executed every time the menu is to be shown
			// its results are destroyed every time the menu is hidden
			// e is the original contextmenu event, containing e.pageX and e.pageY (amongst other data)
			return {
					callback: function(key, options) {

						switch(key) {
							case "all-var":
							allVar();
							break;
							case "sel-var":
							selVar(e);
							break;
							case "all-val":
							allVal();
							break;
							case "all-call":
							allCall();
							break;
							case "all-con":
							allCon();
							break;
							case "erase":
							erase();
							break;
						}

					},
					items: {
						"all": {
							name: "All Effects",
							icon: "fa-bars",
							items: {
								"all-var": {name: "Variables", icon: "fa-bicycle"},
								"all-val": {name: "Values", icon: "fa-fighter-jet"},
								"all-call": {name: "Callsites", icon: "fa-ship"},
								"all-con": {name: "Conditions", icon: "fa-train"}}},
						"selected": {
							name: "Selected Effects",
							icon: "fa-window-minimize",
							items: {
								"sel-var": {name: "Variables", icon: "fa-bicycle"},
								"sel-val": {name: "Values", icon: "fa-fighter-jet"},
								"sel-call": {name: "Callsites", icon: "fa-ship"},
								"sel-con": {name: "Conditions", icon: "fa-train"}}},
						"sep1": "---------",
						"erase": {name: "Remove Highlighting", icon: "fa-eraser"}
					}
			};
		}
	});
});

//$(function(){
//    $.contextMenu({
//        selector: '.context-menu', 
//        build: function($trigger, e) {
//            // this callback is executed every time the menu is to be shown
//            // its results are destroyed every time the menu is hidden
//            // e is the original contextmenu event, containing e.pageX and e.pageY (amongst other data)
//            return {
//                callback: function(key, options) {
//                    var m = "clicked: " + key;
//										//m = $(e.target).attr('class').split(' ');
//										var annotations = e.target.classList;
//										for(var i = 0; i < annotations.length; i++) {
//											switch(annotations[i]) {
//												case "VAL-DEF":
//												case "VAL-USE":
//													m = $(e.target).attr('data-address').split(',');
//													break;
//											}
//										}
//                    window.console && console.log(m) || alert(m); 
//                },
//                items: {
//                    "edit": {name: "Edit", icon: "edit"},
//                    "cut": {name: "Cut", icon: "cut"},
//                    "copy": {name: "Copy", icon: "copy"},
//                    "paste": {name: "Paste", icon: "paste"},
//                    "delete": {name: "Delete", icon: "delete"},
//                    "sep1": "---------",
//                    "quit": {name: "Quit", icon: function($element, key, item){ return 'context-menu-icon context-menu-icon-quit'; }}
//                }
//            };
//        }
//    });
//});

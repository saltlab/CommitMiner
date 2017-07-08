/**
 * Remove all def-use highlighting.
 */
function erase() {
	$('.ENV-DEF').removeClass('def');
	$('.DENV-DEF').removeClass('goto');
	$('.DENV-USE').removeClass('use');
	$('.ENV-USE').removeClass('use');
	$('.VAL-DEF').removeClass('def');
	$('.DVAL-DEF').removeClass('goto');
	$('.DVAL-USE').removeClass('use');
	$('.VAL-USE').removeClass('use');
	$('.CALL-DEF').removeClass('def');
	$('.CALL-USE').removeClass('use');
	$('.CON-DEF').removeClass('def');
	$('.CON-USE').removeClass('use');
}

/**
 * Shows all lines.
 */
function unslice() {
	$('tr.expandable').remove();
	$('tr').show();
}

/**
 * Highlights all def/use spans.
 */
function all(def, use) {
	erase();
	unslice();
	$('.' + def).addClass('def');
	$('.' + use).addClass('use');
}

/**
 * Get the IDs for the selected def/use spans.
 */
function getIDs(e, def, use) {

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

	return ids;

}

/**
 * Get the span element for the selected def/use type.
 */
function getSpanElement(e, def, use) {

	var ids = null;
	var current = $(e.target);
	while(ids === null && current.prop("nodeName") === "SPAN") {

		var annotations = current.attr('class').split(' ');
		for(var i = 0; i < annotations.length; i++) {
			switch(annotations[i]) {
				case def:
				case use:
					return current;
			}
		}

		current = current.parent();
	}

	return ids;

}

/**
 * Highlights the selected def/use spans.
 */
function sel(e, def, use) {

	erase();
	unslice();
	var ids = getIDs(e, def, use);
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
 * Highlights and slices the selected def/use spans.
 */
function slice(e, def, use) {

	erase();
	var ids = getIDs(e, def, use);
	if(ids === null) return;

	$("tr").hide();

	$("span." + use).each(function(index) {
		for(var i = 0; i < ids.length; i++) {
			if($(this).attr('data-address').split(',').indexOf(ids[i]) >= 0) {
				$(this).addClass('use');
				$(this).closest('tr').show();
			}
		}
	});

	$("span." + def).each(function(index) {
		for(var i = 0; i < ids.length; i++) {
			if($(this).attr('data-address').split(',').indexOf(ids[i]) >= 0) {
				$(this).addClass('def');
				$(this).closest('tr').show();
			}
		}
	});

}

/**
 * @return true if all the IDs are the same
 */
function checkIDs(elements) {
	if(elements === null || elements.length <= 1) return true;
	var l = elements[0].attr('data-address').split(',');
	for(var i = 1; i < elements.length; i++) {
		var r = elements[i].attr('data-address').split(',');
		if(l.length !== r.length) return false;
		for(var j = 0; j < r.length; j++) {
			if(l[j] !== r[j]) return false;
		}
	}
	return true;
}

/**
 * Highlight and goto the definition of the selected element.
 */
function gotoDef(e, def, use) {

	erase();
	var ids = getIDs(e, def, use);
	if(ids == null) return;

	var element = getSpanElement(e, def, use);
	element.addClass('use');

	var elements = [];
	$("span." + def).each(function(index) {
		for(var i = 0; i < ids.length; i++) {

			if($(this).attr('data-address').split(',').indexOf(ids[i]) >= 0) {
				$(this).removeClass('use'); $(this).addClass('goto');

				elements.push($(this));
			}

		}
	});

	if(checkIDs(elements)) {
		/* Scroll to the element. */
		$('html, body').animate({
						scrollTop: elements[0].offset().top
				}, 200);
	}
	else if(elements.length > 1 ) {
		/* Slice the definitions. */
		$("tr").hide();
		element.closest('tr').show();
		for(var i = 0; i < elements.length; i++) {
			elements[i].closest('tr').show();
		}
	}

}

/* Set up def/use highlighting when the user left-clicks on a variable or 
 * value. */
function defUse(element, def, use) {

	erase();

	/* Check if this span has a DVAL-DEF or DVAL-USE class. */
	if($(element).attr('data-address') === '') return;
	var ids = $(element).attr('data-address').split(',');

	/* Find and highlight the value definitions. */
	$("span." + def).each(function(index) {
		for(var i = 0; i < ids.length; i++) {

			if($(this).attr('data-address').split(',').indexOf(ids[i]) >= 0) {
				$(this).addClass('goto');
			}

		}
	});

	/* Find and highlight the value uses. */
	$("span." + use).each(function(index) {
		for(var i = 0; i < ids.length; i++) {

			if($(this).attr('data-address').split(',').indexOf(ids[i]) >= 0) {
				$(this).addClass('use');
			}

		}
	});

}

/* Slice to show only the line changes. */
function sliLine() {

	$("tr").hide();

	$("td.insert").each(function() {
		var tr = $(this).closest("tr");
		tr.prev().prev().show();
		tr.prev().show();
		tr.show();
		tr.next().show();
		tr.next().next().show();
	});

	$("td.delete").each(function() {
		var tr = $(this).closest("tr");
		tr.prev().prev().show();
		tr.prev().show();
		tr.show().show();
		tr.next().show();
		tr.next().next().show();
	});

	placeholders();

}

/* Add placeholder rows to show where rows have been hidden. */
function placeholders() {

	var current = $("tr:hidden").first();

	while(current.length > 0) {

		current.before("<tr class='code expandable'><td class='expandable-line'><i class='fa fa-compress' style='font-size:18px;'></i></td><td class='expandable-blob' colspan='3'></td></tr>");

		while(current.is(":hidden")) {
			current = current.next();
		}

		while(current.is(":visible")) {
			current = current.next();
		}
		
	}

}

function allVar() {	all('ENV-DEF', 'ENV-USE'); }
function selVar(e) { sel(e, "ENV-DEF", "ENV-USE"); }
function sliVar(e) { slice(e, "ENV-DEF", "ENV-USE"); }
function allVal() { all('VAL-DEF', 'VAL-USE'); }
function selVal(e) { sel(e, "VAL-DEF", "VAL-USE"); }
function sliVal(e) { slice(e, "VAL-DEF", "VAL-USE"); }
function allCall() { all('CALL-DEF', 'CALL-USE'); }
function selCall(e) { sel(e, "CALL-DEF", "CALL-USE"); }
function sliCall(e) { slice(e, "CALL-DEF", "CALL-USE"); }
function allCon() { all('CON-DEF', 'CON-USE'); }
function selCon(e) { sel(e, "CON-DEF", "CON-USE"); }
function sliCon(e) { slice(e, "CON-DEF", "CON-USE"); }
function gotoVar(e) { gotoDef(e, "DENV-DEF", "DENV-USE"); }
function gotoVal(e) { gotoDef(e, "DVAL-DEF", "DVAL-USE"); }

$("span.DENV-USE, span.DENV-DEF").click(function(e) { 
	if(!e.altKey) { 
		defUse(this, "DENV-DEF", "DENV-USE"); 
	} 
});

$("span.DVAL-USE, span.DVAL-DEF").click(function(e) { 
	if(e.altKey) { 
		defUse(this, "DVAL-DEF", "DVAL-USE"); 
	} 
});

/* Startup. */
$(function() {
	sliLine();
});

/* Set up the context menu. */
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
							case "sli-var":
							sliVar(e);
							break;
							case "all-val":
							allVal();
							break;
							case "sel-val":
							selVal(e);
							break;
							case "sli-val":
							sliVal(e);
							break;
							case "all-call":
							allCall();
							break;
							case "sel-call":
							selCall(e);
							break;
							case "sli-call":
							sliCall(e);
							break;
							case "all-con":
							allCon();
							break;
							case "sel-con":
							selCon(e);
							break;
							case "sli-con":
							sliCon(e);
							break;
							case "goto-var":
							gotoVar(e);
							break;
							case "goto-val":
							gotoVal(e);
							break;
							case "sli-line":
							sliLine();
							break;
							case "erase":
							erase();
							break;
							case "unslice":
							unslice();
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
							icon: "fa-i-cursor",
							items: {
								"sel-var": {name: "Variables", icon: "fa-bicycle"},
								"sel-val": {name: "Values", icon: "fa-fighter-jet"},
								"sel-call": {name: "Callsites", icon: "fa-ship"},
								"sel-con": {name: "Conditions", icon: "fa-train"}}},
						"slice": {
							name: "Slice",
							icon: "fa-scissors",
							items: {
								"sli-var": {name: "Variables", icon: "fa-bicycle"},
								"sli-val": {name: "Values", icon: "fa-fighter-jet"},
								"sli-call": {name: "Callsites", icon: "fa-ship"},
								"sli-con": {name: "Conditions", icon: "fa-train"},
								"sli-line": {name: "Line", icon: "fa-taxi"}}},
						"goto": {
							name: "Goto Def",
							icon: "fa-sign-in",
							items: {
								"goto-var": {name: "Variable Def", icon: "fa-bicycle"},
								"goto-val": {name: "Value Def", icon: "fa-fighter-jet"}}},
						"sep1": "---------",
						"erase": {name: "Remove Highlighting", icon: "fa-eraser"},
						"unslice": {name: "Undo Slice", icon: "fa-undo"}
					}
			};
		}
	});
});

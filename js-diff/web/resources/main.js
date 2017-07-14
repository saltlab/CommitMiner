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
	$('.CONDEP-DEF').removeClass('def');
	$('.CONDEP-USE').removeClass('use');
	$('.DATDEP-XDEF').removeClass('def');
	$('.DATDEP-USE').removeClass('use');
}

/**
 * Shows all lines.
 */
function unslice() {
	$('tr.expandable').remove();
	$('tr').show();
}

/**
 * Hide all rows.
 */
function hideRows() {
	$("tr").hide();
}

/**
 * Display the rows around the given element.
 */
function showContext() {
	var tr = $(this).closest("tr");
	tr.prev().prev().show();
	tr.prev().show();
	tr.show();
	tr.next().show();
	tr.next().next().show();
}

/**
 * Highlights all def/use spans.
 */
function all(def, use) {

	erase();
	unslice();
	hideRows();

	$('.' + def).addClass('def').each(showContext);
	$('.' + use).addClass('use').each(showContext);

	addPlaceholders();

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
	unslice();

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
		hideRows();
		element.closest('tr').show().each(showContext);
		for(var i = 0; i < elements.length; i++) {
			elements[i].closest('tr').each(showContext);
		}
		addPlaceholders();
	}

}

/* Set up def/use highlighting when the user left-clicks on a variable or 
 * value. */
function defUse(element, def, use, slice) {

	if(element.length == 0) return;

	erase();

	if(slice) {
		unslice();
		hideRows();
	}

	/* Check if this span has a DVAL-DEF or DVAL-USE class. */
	if(element.attr('data-address') === '') return;
	var ids = element.attr('data-address').split(',');

	/* Find and highlight the value definitions. */
	$("span." + def).each(function(index) {
		for(var i = 0; i < ids.length; i++) {

			if($(this).attr('data-address').split(',').indexOf(ids[i]) >= 0) {
				$(this).addClass('goto');
				if(slice) $(this).closest('tr').each(showContext);
			}

		}
	});

	/* Find and highlight the value uses. */
	$("span." + use).each(function(index) {
		for(var i = 0; i < ids.length; i++) {

			if($(this).attr('data-address').split(',').indexOf(ids[i]) >= 0) {
				$(this).addClass('use');
				if(slice) $(this).closest('tr').each(showContext);
			}

		}
	});

	if(slice) addPlaceholders();

}

/* Slice to show only the line changes. */
function sliLine() {

	$("tr").hide();
	$("td.insert").each(showContext);
	$("td.delete").each(showContext);
	addPlaceholders();

}

/* Add placeholder rows to show where rows have been hidden. */
function addPlaceholders() {

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

/* Finds uses of the variable or value. */
function findUses(e, def, use) {
	defUse($(e.target).closest("span." + def + ", span." + use), def, use, true);
}

function allVar() {	all('ENV-DEF', 'ENV-USE'); }
function allVal() { all('VAL-DEF', 'VAL-USE'); }
function allCall() { all('CALL-DEF', 'CALL-USE'); }
function allCon() { all('CON-DEF', 'CON-USE'); }
function allConDep() { all('CONDEP-DEF', 'CONDEP-USE'); }
function allDatDep() { all('DATDEP-XDEF', 'DATDEP-USE'); }
function gotoVar(e) { gotoDef(e, "DENV-DEF", "DENV-USE"); }
function gotoVal(e) { gotoDef(e, "DVAL-DEF", "DVAL-USE"); }

function findVar(e) { findUses(e, "DENV-DEF", "DENV-USE"); }
function findVal(e) { findUses(e, "DVAL-DEF", "DVAL-USE"); }

$("span.DENV-USE, span.DENV-DEF").click(function(e) { 
	if(!e.altKey) { 
		defUse($(e.target).closest("span.DENV-DEF, span.DENV-USE"), "DENV-DEF", "DENV-USE");
	} 
});

$("span.DVAL-USE, span.DVAL-DEF").click(function(e) { 
	if(e.altKey) { 
		defUse($(e.target).closest("span.DVAL-DEF, span.DVAL-USE"), "DVAL-DEF", "DVAL-USE");
	} 
});

/* Startup. */
$(function() {
	sliLine();
});

/* Switch context menu selections. */
function switchMenuSelection(key, options, e) {

	switch(key) {
		case "all-var":
		allVar();
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
		case "all-condep":
		allConDep();
		break;
		case "all-datdep":
		allDatDep();
		break;
		case "goto-var":
		gotoVar(e);
		break;
		case "goto-val":
		gotoVal(e);
		break;
		case "find-var":
		findVar(e);
		break;
		case "find-val":
		findVal(e);
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

}

/* The menu for MultiDiff change impact analysis. */
function getMultiDiffMenu() {
	return {	name: "Change Impact",
						icon: "fa-bars",
						items: {
							"all-var": {name: "Variables", icon: "fa-bicycle"},
							"all-val": {name: "Values", icon: "fa-fighter-jet"},
							"all-call": {name: "Callsites", icon: "fa-ship"},
							"all-con": {name: "Conditions", icon: "fa-train"},
							"sli-line": {name: "Line", icon: "fa-taxi"}}};
}

/* The menu for data/control dependency change impact analysis. */
function getDependencyMenu() {
	return {	name: "Change Impact",
						icon: "fa-bars",
						items: {
							"all-condep": {name: "Control Dependencies", icon: "fa-bicycle"},
							"all-datdep": {name: "Data Dependencies", icon: "fa-fighter-jet"}}};
}

function getVariableGotoMenu() {
	return {	name: "Goto Definition",
						icon: "fa-sign-in",
						items: {
							"goto-var": {name: "Variable Definition", icon: "fa-bicycle"},
							"goto-val": {name: "Value Definition", icon: "fa-fighter-jet"}}};
}

function getVariableFindMenu() { 
	return {	name: "Find All Uses",
						icon: "fa-search",
						items: {
							"find-var": {name: "Variable Uses", icon: "fa-bicycle"},
							"find-val": {name: "Value Uses", icon: "fa-fighter-jet"}}};
}

function getValueGotoMenu() {
	return {	name: "Goto Definition",
						icon: "fa-sign-in",
						items: {
							"goto-val": {name: "Value Definition", icon: "fa-fighter-jet"}}};
}

function getValueFindMenu() { 
	return {	name: "Find All Uses",
						icon: "fa-search",
						items: {
							"find-val": {name: "Value Uses", icon: "fa-fighter-jet"}}};
}

/* Set up the context menu. */
$(function() {
	$.contextMenu({
		selector: '.context-menu', 
		build: function($trigger, e) {
			// this callback is executed every time the menu is to be shown
			// its results are destroyed every time the menu is hidden
			// e is the original contextmenu event, containing e.pageX and e.pageY (amongst other data)

			/* Select the correct change impact menu options. */
			var changeImpactMenu = null;
			if($("span.CONDEP-DEF, span.CONDEF-USE, span.DATDEF-DEF, span.DATDEF-USE").first().length > 0)
				changeImpactMenu = getDependencyMenu();
			else 
				changeImpactMenu = getMultiDiffMenu();

			/* Build the menu based on the context. */
			if($(e.target).closest("span.DENV-DEF, span.DENV-USE").length > 0) {
				return {
						callback: function (key, options) { switchMenuSelection(key, options, e) },
						items: {
							"all": changeImpactMenu,
							"goto": getVariableGotoMenu(),
							"find": getVariableFindMenu(),
							"sep1": "---------",
							"erase": {name: "Remove Highlighting", icon: "fa-eraser"},
							"unslice": {name: "Undo Slice", icon: "fa-undo"}
						}
				};
			}
			else if($(e.target).closest("span.DVAL-DEF, span.DVAL-USE").length > 0) {
				return {
						callback: function (key, options) { switchMenuSelection(key, options, e) },
						items: {
							"all": changeImpactMenu,
							"goto": getValueGotoMenu(),
							"find": getValueFindMenu(),
							"sep1": "---------",
							"erase": {name: "Remove Highlighting", icon: "fa-eraser"},
							"unslice": {name: "Undo Slice", icon: "fa-undo"}
						}
				};
			}
			else {
				return {
						callback: function (key, options) { switchMenuSelection(key, options, e) },
						items: {
							"all": changeImpactMenu,
							"sep1": "---------",
							"erase": {name: "Remove Highlighting", icon: "fa-eraser"},
							"unslice": {name: "Undo Slice", icon: "fa-undo"}
						}
				};
			}
		}
	});
});

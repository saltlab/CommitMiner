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
 * Shows all lines.
 */
function unslice() {
	$('tr').show();
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
 * Highlights the selected def/use spans.
 */
function sel(e, def, use) {

	erase();
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
							name: "Sliced Effects",
							icon: "fa-scissors",
							items: {
								"sli-var": {name: "Variables", icon: "fa-bicycle"},
								"sli-val": {name: "Values", icon: "fa-fighter-jet"},
								"sli-call": {name: "Callsites", icon: "fa-ship"},
								"sli-con": {name: "Conditions", icon: "fa-train"}}},
						"sep1": "---------",
						"erase": {name: "Remove Highlighting", icon: "fa-eraser"},
						"unslice": {name: "Undo Slice", icon: "fa-undo"}
					}
			};
		}
	});
});

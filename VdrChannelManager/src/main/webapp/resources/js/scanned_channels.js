// Return true if at least one of the channel checkboxes within the table is checked
function isAtLeastOneChannelCheckboxChecked() {
	var result = false;
	var checkboxNameSuffix = 'channelCheckbox';
	var elements = document.getElementsByTagName('input');

	for (var i = 0; i < elements.length; ++i) {
		if (elements[i].type == 'checkbox') {
			if (elements[i].name.indexOf(checkboxNameSuffix,
					elements[i].name.length - checkboxNameSuffix.length) !== -1) {
				if (elements[i].checked) {
					result = true;
					break;
				}
			}
		}
	}

	return result;
}

// Return true if exactly one of the channel checkboxes within the table is
// checked
function isExactlyOneChannelCheckboxChecked() {
	var result = false;
	var checkboxNameSuffix = 'channelCheckbox';
	var elements = document.getElementsByTagName('input');
	var checkedCount = 0;

	for (var i = 0; i < elements.length; ++i) {
		if (elements[i].type == 'checkbox') {
			if (elements[i].name.indexOf(checkboxNameSuffix,
					elements[i].name.length - checkboxNameSuffix.length) !== -1) {
				if (elements[i].checked) {
					++checkedCount;
				}
			}
		}
	}

	if (checkedCount == 1) {
		result = true;
	}

	return result;
}

//The Add To Main List panel form key press listener
//Emulates 'OK' button click on hitting the 'Enter' key
//Emulates 'Cancel' button click on hitting the 'Esc' key
//Loops input focus through the form inputs on pressing 'Tab' / 'Shift+Tab'
function addToChannelsPanelFormKeyPressHandler(event) {
	var result;
	var element;

	result = true;

	event = event || window.event;

	if (event.keyCode == 13) {
		if (document.activeElement != document
				.getElementById('addToChannelsPanelForm:cancelButton')) {
			element = document.getElementById('addToChannelsPanelForm:okAddButton');
			if (element == null) {
				element = document.getElementById('addToChannelsPanelForm:okNoTransponderButton');
			}
			if (element == null) {
				element = document.getElementById('addToChannelsPanelForm:okNoSourceButton');
			}
			element.click();
			result = false;
		}
	}

	if (event.keyCode == 27) {
		document.getElementById('addToChannelsPanelForm:cancelButton').click();
		result = false;
	}

	if (event.keyCode == 9) {
		if (event.shiftKey) {
			if (document.activeElement == document
					.getElementById('addToChannelsPanelForm:nameInput')) {
				document.getElementById('addToChannelsPanelForm:cancelButton').focus();
				result = false;
			}
		} else {
			if (document.activeElement == document
					.getElementById('addToChannelsPanelForm:cancelButton')) {
				document.getElementById('addToChannelsPanelForm:nameInput').focus();
				result = false;
			}
		}
	}

	return result;
}

//The Add To Ignored panel form key press listener
//Emulates 'Cancel' button click on hitting the 'Esc' key
//Loops input focus through 'OK' and 'Cancel' buttons on pressing 'Tab' /
//'Shift+Tab'
function addToIgnoredChannelsPanelFormKeyPressHandler(event) {
	var result;

	result = true;

	event = event || window.event;

	if (event.keyCode == 27) {
		document.getElementById('addToIgnoredChannelsPanelForm:cancelButton').click();
		result = false;
	}

	if (event.keyCode == 9) {
		if (event.shiftKey) {
			if (document.activeElement == document
					.getElementById('addToIgnoredChannelsPanelForm:okAddButton')) {
				document.getElementById('addToIgnoredChannelsPanelForm:cancelButton').focus();
				result = false;
			}
		} else {
			if (document.activeElement == document
					.getElementById('addToIgnoredChannelsPanelForm:cancelButton')) {
				document.getElementById('addToIgnoredChannelsPanelForm:okAddButton').focus();
				result = false;
			}
		}
	}

	return result;
}

//The Update In Main List panel form key press listener
//Emulates 'OK' button click on hitting the 'Enter' key
//Emulates 'Cancel' button click on hitting the 'Esc' key
//Loops input focus through the form inputs on pressing 'Tab' / 'Shift+Tab'
function updateInChannelsPanelFormKeyPressHandler(event) {
	var result;

	result = true;

	event = event || window.event;

	if (event.keyCode == 13) {
		if (document.activeElement != document
				.getElementById('updateInChannelsPanelForm:cancelButton')) {
			document.getElementById('updateInChannelsPanelForm:okUpdateButton').click();
			result = false;
		}
	}

	if (event.keyCode == 27) {
		document.getElementById('updateInChannelsPanelForm:cancelButton').click();
		result = false;
	}

	if (event.keyCode == 9) {
		if (event.shiftKey) {
			if (document.activeElement == document
					.getElementById('updateInChannelsPanelForm:nameInput')) {
				document.getElementById('updateInChannelsPanelForm:cancelButton').focus();
				result = false;
			}
		} else {
			if (document.activeElement == document
					.getElementById('updateInChannelsPanelForm:cancelButton')) {
				document.getElementById('updateInChannelsPanelForm:nameInput').focus();
				result = false;
			}
		}
	}

	return result;
}

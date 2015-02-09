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

// Return true if all channel checkboxes within the table are checked
function areAllChannelCheckboxesChecked() {
	var result = true;
	var checkboxNameSuffix = 'channelCheckbox';
	var elements = document.getElementsByTagName('input');

	for (var i = 0; i < elements.length; ++i) {
		if (elements[i].type == 'checkbox') {
			if (elements[i].name.indexOf(checkboxNameSuffix,
					elements[i].name.length - checkboxNameSuffix.length) !== -1) {
				if (!elements[i].checked) {
					result = false;
					break;
				}
			}
		}
	}

	return result;
}

// Check/uncheck all channel checkboxes within the table depending on 'value'
// parameter
function toggleAllChannelCheckboxes(value) {
	var checkboxNameSuffix = 'channelCheckbox';
	var elements = document.getElementsByTagName('input');

	for (var i = 0; i < elements.length; ++i) {
		if (elements[i].type == 'checkbox') {
			if (elements[i].name.indexOf(checkboxNameSuffix,
					elements[i].name.length - checkboxNameSuffix.length) !== -1) {
				elements[i].checked = value;
			}
		}
	}
}

// The Remove panel form key press listener
// Emulates 'Cancel' button click on hitting the 'Esc' key
// Loops input focus through 'OK' and 'Cancel' buttons on pressing 'Tab' /
// 'Shift+Tab'
function removePanelFormKeyPressHandler(event) {
	var result;

	result = true;

	event = event || window.event;

	if (event.keyCode == 27) {
		document.getElementById('removePanelForm:cancelButton').click();
		result = false;
	}

	if (event.keyCode == 9) {
		if (event.shiftKey) {
			if (document.activeElement == document
					.getElementById('removePanelForm:okButton')) {
				document.getElementById('removePanelForm:cancelButton').focus();
				result = false;
			}
		} else {
			if (document.activeElement == document
					.getElementById('removePanelForm:cancelButton')) {
				document.getElementById('removePanelForm:okButton').focus();
				result = false;
			}
		}
	}

	return result;
}

// The Sort panel form key press listener
// Emulates 'OK' button click on hitting the 'Enter' key
// Emulates 'Cancel' button click on hitting the 'Esc' key
// Loops input focus through the form inputs on pressing 'Tab' / 'Shift+Tab'
function sortPanelFormKeyPressHandler(event) {
	var result;
	var element;

	result = true;

	event = event || window.event;

	if (event.keyCode == 13) {
		if (document.activeElement != document
				.getElementById('sortPanelForm:cancelButton')) {
			element = document.getElementById('sortPanelForm:okButton');
			element.click();
			result = false;
		}
	}

	if (event.keyCode == 27) {
		document.getElementById('sortPanelForm:cancelButton').click();
		result = false;
	}

	if (event.keyCode == 9) {
		if (event.shiftKey) {
			if (document.activeElement == document
					.getElementById('sortPanelForm:sortModeMenu')) {
				document.getElementById('sortPanelForm:cancelButton').focus();
				result = false;
			}
		} else {
			if (document.activeElement == document
					.getElementById('sortPanelForm:cancelButton')) {
				document.getElementById('sortPanelForm:sortModeMenu').focus();
				result = false;
			}
		}
	}

	return result;
}

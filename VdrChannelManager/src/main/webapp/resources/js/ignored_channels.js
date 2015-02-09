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

// The Update panel form key press listener
// Emulates 'Cancel' button click on hitting the 'Esc' key
// Loops input focus through 'OK' and 'Cancel' buttons on pressing 'Tab' /
// 'Shift+Tab'
function updatePanelFormKeyPressHandler(event) {
	var result;

	result = true;

	event = event || window.event;

	if (event.keyCode == 27) {
		document.getElementById('updatePanelForm:cancelButton').click();
		result = false;
	}

	if (event.keyCode == 9) {
		if (event.shiftKey) {
			if (document.activeElement == document
					.getElementById('updatePanelForm:okButton')) {
				document.getElementById('updatePanelForm:cancelButton').focus();
				result = false;
			}
		} else {
			if (document.activeElement == document
					.getElementById('updatePanelForm:cancelButton')) {
				document.getElementById('updatePanelForm:okButton').focus();
				result = false;
			}
		}
	}

	return result;
}

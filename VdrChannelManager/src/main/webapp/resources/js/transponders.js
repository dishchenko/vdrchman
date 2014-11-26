function isAtLeastOneTransponderCheckboxChecked() {
	var result = false;
	var checkboxNameSuffix = 'transponderCheckbox';
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

function isExactlyOneTransponderCheckboxChecked() {
	var result = false;
	var checkboxNameSuffix = 'transponderCheckbox';
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

function editPanelFormKeyPressHandler(event) {
	var result;
	var element;

	result = true;

	event = event || window.event;

	if (event.keyCode == 13) {
		if (document.activeElement != document
				.getElementById('editPanelForm:cancelButton')) {
			element = document.getElementById('editPanelForm:okAddButton');
			if (element == null) {
				element = document.getElementById('editPanelForm:okEditButton');
			}
			element.click();
			result = false;
		}
	}

	if (event.keyCode == 27) {
		document.getElementById('editPanelForm:cancelButton').click();
		result = false;
	}

	if (event.keyCode == 9) {
		if (event.shiftKey) {
			if (document.activeElement == document
					.getElementById('editPanelForm:sourcesMenu')) {
				document.getElementById('editPanelForm:cancelButton').focus();
				result = false;
			}
			if (document.activeElement == document
					.getElementById('editPanelForm:frequencyInput')) {
				element = document.getElementById('editPanelForm:sourcesMenu');
				if (element != null) {
					element.focus();
				} else {
					document.getElementById('editPanelForm:cancelButton')
							.focus();
				}
				result = false;
			}
		} else {
			if (document.activeElement == document
					.getElementById('editPanelForm:cancelButton')) {
				element = document.getElementById('editPanelForm:sourcesMenu');
				if (element != null) {
					element.focus();
				} else {
					document.getElementById('editPanelForm:frequencyInput')
							.focus();
				}
				result = false;
			}
		}
	}

	return result;
}

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

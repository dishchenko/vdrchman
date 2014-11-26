package di.vdrchman.util;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.inject.Named;

@Named
public class FormResetter {

	public static void resetForm(UIComponent form) {
		for (UIComponent uic : form.getChildren()) {
			if (uic instanceof EditableValueHolder) {
				EditableValueHolder evh = (EditableValueHolder) uic;
				evh.resetValue();
			}
			resetForm(uic);
		}
	}

}

package di.vdrchman.data;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import di.vdrchman.model.Transponder;

@ManagedBean
@ViewScoped
public class TransponderSourceFrequencyPolarityUniquenessValidator implements
		Validator {

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private TranspondersManager transpondersManager;

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		Object sourceAttrValue;
		Integer newSourceId;
		Object frequencyAttrValue;
		Integer newFrequency;
		Object polarityAttrValue;
		String newPolarity;
		Transponder editedTransponder;
		Transponder foundTransponder;
		Integer editedTransponderId;

		editedTransponder = transpondersManager.getEditedTransponder();

		sourceAttrValue = ((UIInput) context.getViewRoot().findComponent(
				(String) component.getAttributes().get("source")))
				.getSubmittedValue();
		if (sourceAttrValue != null) {
			try {
				newSourceId = Integer.valueOf((String) sourceAttrValue);
			} catch (NumberFormatException ex) {
				newSourceId = -1;
			}
		} else {
			if (editedTransponder.getSourceId() != null) {
				newSourceId = editedTransponder.getSourceId();
			} else {
				newSourceId = transpondersManager.getFilteredSourceId();
			}
		}

		frequencyAttrValue = ((UIInput) context.getViewRoot().findComponent(
				(String) component.getAttributes().get("frequency")))
				.getSubmittedValue();
		try {
			newFrequency = Integer.valueOf((String) frequencyAttrValue);
		} catch (NumberFormatException ex) {
			newFrequency = -1;
		}

		polarityAttrValue = ((UIInput) context.getViewRoot().findComponent(
				(String) component.getAttributes().get("polarity")))
				.getSubmittedValue();
		newPolarity = (String) polarityAttrValue;

		if ((newSourceId != null) && (newFrequency != null)
				&& (newPolarity != null)) {
			foundTransponder = transponderRepository
					.findBySourceFrequencyPolarity(newSourceId, newFrequency,
							newPolarity);
			if (foundTransponder != null) {
				editedTransponderId = editedTransponder.getId();
				if ((editedTransponderId == null)
						|| !foundTransponder.getId()
								.equals(editedTransponderId)) {
					FacesMessage msg = new FacesMessage(
							"Transponder parameters uniqueness validation failed",
							"A Transponder with such Source, Frequency and Polarity already exists");
					msg.setSeverity(FacesMessage.SEVERITY_ERROR);
					throw new ValidatorException(msg);
				}
			}
		}
	}

}

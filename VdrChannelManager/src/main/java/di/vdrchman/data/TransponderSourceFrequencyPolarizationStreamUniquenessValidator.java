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
public class TransponderSourceFrequencyPolarizationStreamUniquenessValidator implements
		Validator {

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private TranspondersManager transpondersManager;

	// Validates that there would not be two transponders with the same
	// frequency and polarization within the source given
	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		Object sourceIdAttrValue;
		Long newSourceId;
		Object frequencyAttrValue;
		Integer newFrequency;
		Object polarizationAttrValue;
		String newPolarization;
		Object streamIdAttrValue;
		Integer newStreamId;
		Transponder editedTransponder;
		Transponder foundTransponder;
		Long editedTranspId;

		editedTransponder = transpondersManager.getEditedTransponder();

		sourceIdAttrValue = ((UIInput) context.getViewRoot().findComponent(
				(String) component.getAttributes().get("sourceId")))
				.getSubmittedValue();
		if (sourceIdAttrValue != null) {
			try {
				newSourceId = Long.valueOf((String) sourceIdAttrValue);
			} catch (NumberFormatException ex) {
				newSourceId = -1L;
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

		polarizationAttrValue = ((UIInput) context.getViewRoot().findComponent(
				(String) component.getAttributes().get("polarization")))
				.getSubmittedValue();
		newPolarization = (String) polarizationAttrValue;

		streamIdAttrValue = ((UIInput) context.getViewRoot().findComponent(
				(String) component.getAttributes().get("streamId")))
				.getSubmittedValue();
		if (streamIdAttrValue != null) {
			try {
				newStreamId = Integer.valueOf((String) streamIdAttrValue);
			} catch (NumberFormatException ex) {
				newStreamId = null;
			}
		} else {
			newStreamId = null;
		}

		if ((newSourceId != null) && (newFrequency != null)
				&& (newPolarization != null)) {
			foundTransponder = transponderRepository
					.findBySourceFrequencyPolarizationStream(newSourceId,
							newFrequency, newPolarization, newStreamId);
			if (foundTransponder != null) {
				editedTranspId = editedTransponder.getId();
				if ((editedTranspId == null)
						|| !foundTransponder.getId().equals(editedTranspId)) {
					FacesMessage msg = new FacesMessage(
							"Transponder parameters uniqueness validation failed",
							"A transponder with such source, frequency, polarization and stream ID already exists");
					msg.setSeverity(FacesMessage.SEVERITY_ERROR);
					throw new ValidatorException(msg);
				}
			}
		}
	}

}

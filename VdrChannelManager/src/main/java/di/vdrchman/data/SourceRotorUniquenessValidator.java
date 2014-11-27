package di.vdrchman.data;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import di.vdrchman.model.Source;

@ManagedBean
@ViewScoped
public class SourceRotorUniquenessValidator implements Validator {

	@Inject
	private SourceRepository sourceRepository;

	@Inject
	private SourcesManager sourcesManager;

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		Integer newRotor;
		Source foundSource;
		Long editedSourceId;

		newRotor = (Integer) value;
		if (newRotor != null) {
			foundSource = sourceRepository.findByRotor(newRotor);
			if (foundSource != null) {
				editedSourceId = sourcesManager.getEditedSource().getId();
				if ((editedSourceId == null)
						|| !foundSource.getId().equals(editedSourceId)) {
					FacesMessage msg = new FacesMessage(
							"Source rotor position uniqueness validation failed",
							"A Source with such Rotor position already exists");
					msg.setSeverity(FacesMessage.SEVERITY_ERROR);
					throw new ValidatorException(msg);
				}
			}
		}
	}

}

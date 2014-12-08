package di.vdrchman.data;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import di.vdrchman.model.Group;

@ManagedBean
@ViewScoped
public class GroupStartChannelNoUniquenessValidator implements Validator {

	@Inject
	private GroupRepository groupRepository;

	@Inject
	private GroupsManager groupsManager;

	// Validates that a user would not have two groups with the same starting
	// channel number
	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		Integer newStartChannelNo;
		Group foundGroup;
		Long editedGroupId;

		newStartChannelNo = (Integer) value;
		if (newStartChannelNo != null) {
			foundGroup = groupRepository.findByStartChannelNo(newStartChannelNo);
			if (foundGroup != null) {
				editedGroupId = groupsManager.getEditedGroup().getId();
				if ((editedGroupId == null)
						|| !foundGroup.getId().equals(editedGroupId)) {
					FacesMessage msg = new FacesMessage(
							"Group starting channel number uniqueness validation failed",
							"A group with such starting channel number already exists");
					msg.setSeverity(FacesMessage.SEVERITY_ERROR);
					throw new ValidatorException(msg);
				}
			}
		}
	}

}

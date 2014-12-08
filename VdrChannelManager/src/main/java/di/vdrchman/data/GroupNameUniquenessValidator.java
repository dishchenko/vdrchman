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
public class GroupNameUniquenessValidator implements Validator {

	@Inject
	private GroupRepository groupRepository;

	@Inject
	private GroupsManager groupsManager;

	// Validates that a user would not have two groups with the same name
	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		String newName;
		Group foundGroup;
		Long editedGroupId;

		newName = (String) value;
		if (newName != null) {
			foundGroup = groupRepository.findByName(newName);
			if (foundGroup != null) {
				editedGroupId = groupsManager.getEditedGroup().getId();
				if ((editedGroupId == null)
						|| !foundGroup.getId().equals(editedGroupId)) {
					FacesMessage msg = new FacesMessage(
							"Group name uniqueness validation failed",
							"A group with such name already exists");
					msg.setSeverity(FacesMessage.SEVERITY_ERROR);
					throw new ValidatorException(msg);
				}
			}
		}
	}

}

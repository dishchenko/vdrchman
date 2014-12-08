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

import di.vdrchman.model.Channel;

@ManagedBean
@ViewScoped
public class ChannelTransponderSidApidUniquenessValidator implements
		Validator {

	@Inject
	private ChannelRepository channelRepository;

	@Inject
	private ChannelsManager channelsManager;

	// Validates that there would not be two channels with the same
	// SID and APID within the transponder given
	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		Object transponderAttrValue;
		Long newTranspId;
		Object sidAttrValue;
		Integer newSid;
		Object apidAttrValue;
		Integer newApid;
		Channel editedChannel;
		Channel foundChannel;
		Long editedChannelId;

		editedChannel = channelsManager.getEditedChannel();

		transponderAttrValue = ((UIInput) context.getViewRoot().findComponent(
				(String) component.getAttributes().get("transponder")))
				.getSubmittedValue();
		if (transponderAttrValue != null) {
			try {
				newTranspId = Long.valueOf((String) transponderAttrValue);
			} catch (NumberFormatException ex) {
				newTranspId = -1L;
			}
		} else {
			if (editedChannel.getTranspId() != null) {
				newTranspId = editedChannel.getTranspId();
			} else {
				newTranspId = channelsManager.getFilteredTranspId();
			}
		}

		sidAttrValue = ((UIInput) context.getViewRoot().findComponent(
				(String) component.getAttributes().get("sid")))
				.getSubmittedValue();
		try {
			newSid = Integer.valueOf((String) sidAttrValue);
		} catch (NumberFormatException ex) {
			newSid = -1;
		}

		apidAttrValue = ((UIInput) context.getViewRoot().findComponent(
				(String) component.getAttributes().get("apid")))
				.getSubmittedValue();
		try {
			newApid = Integer.valueOf((String) apidAttrValue);
		} catch (NumberFormatException ex) {
			newApid = -1;
		}

		if ((newTranspId != null) && (newSid != null)
				&& (newApid != null)) {
			foundChannel = channelRepository
					.findByTransponderSidApid(newTranspId, newSid,
							newApid);
			if (foundChannel != null) {
				editedChannelId = editedChannel.getId();
				if ((editedChannelId == null)
						|| !foundChannel.getId()
								.equals(editedChannelId)) {
					FacesMessage msg = new FacesMessage(
							"Channel parameters uniqueness validation failed",
							"A channel with such transponder, SID and APID already exists");
					msg.setSeverity(FacesMessage.SEVERITY_ERROR);
					throw new ValidatorException(msg);
				}
			}
		}
	}

}

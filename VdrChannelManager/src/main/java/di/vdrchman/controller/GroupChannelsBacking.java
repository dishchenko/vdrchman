package di.vdrchman.controller;

import javax.enterprise.inject.Model;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.ChannelRepository;
import di.vdrchman.data.GroupChannelsManager;
import di.vdrchman.model.Channel;

@Model
public class GroupChannelsBacking {

	@Inject
	private GroupChannelsManager groupChannelsManager;

	@Inject
	private ChannelRepository channelRepository;

	// The user is going to remove some checked channels
	public void intendRemoveChannels() {
		groupChannelsManager.collectCheckedChannels();
	}

	// Do that removal
	public void doRemoveChannels() {
		for (Channel channel : groupChannelsManager.getCheckedChannels()) {
			channelRepository.delete(channel.getId(),
					groupChannelsManager.getShownGroupId());
		}
		groupChannelsManager.retrieveAllChannels();
		groupChannelsManager.clearCheckedChannels();
		groupChannelsManager.clearChannelCheckboxes();
	}

	// Let's take the checked channel's data on the "clipboard"
	public void takeChannel() {
		groupChannelsManager.collectCheckedChannels();
		groupChannelsManager.setTakenChannel(new Channel(groupChannelsManager
				.getCheckedChannels().get(0)));
		groupChannelsManager.clearCheckedChannels();
		groupChannelsManager.clearChannelCheckboxes();
	}

	// Remember the channel taken on the "clipboard"?
	// Move the original one on top of the current list
	public void moveChannelOnTop() {
		channelRepository.move(groupChannelsManager.getTakenChannel().getId(),
				groupChannelsManager.getShownGroupId(),
				groupChannelsManager.calculateOnTopSeqno());
		groupChannelsManager.retrieveAllChannels();
		groupChannelsManager.clearCheckedChannels();
		groupChannelsManager.clearChannelCheckboxes();
		groupChannelsManager.turnScrollerPage(groupChannelsManager
				.getTakenChannel());
	}

	// Now move the channel taken on the "clipboard" right after
	// the checked channel in the list
	public void moveChannelAfter() {
		int curSeqno;
		int newSeqno;

		groupChannelsManager.collectCheckedChannels();
		curSeqno = channelRepository.getSeqno(groupChannelsManager
				.getTakenChannel().getId(), groupChannelsManager
				.getShownGroupId());
		newSeqno = channelRepository.getSeqno(groupChannelsManager
				.getCheckedChannels().get(0).getId(),
				groupChannelsManager.getShownGroupId());
		if (newSeqno < curSeqno) {
			++newSeqno;
		}
		channelRepository.move(groupChannelsManager.getTakenChannel().getId(),
				groupChannelsManager.getShownGroupId(), newSeqno);
		groupChannelsManager.retrieveAllChannels();
		groupChannelsManager.clearCheckedChannels();
		groupChannelsManager.clearChannelCheckboxes();
		groupChannelsManager.turnScrollerPage(groupChannelsManager
				.getTakenChannel());
	}

	// On changing the group menu selection clear the "clipboard" and turn the
	// scroller to the first page
	public void onGroupMenuSelection(ValueChangeEvent event) {
		groupChannelsManager.setShownGroupId((Long) event.getNewValue());
		groupChannelsManager.setTakenChannel(null);
		groupChannelsManager.retrieveAllChannels();
		groupChannelsManager.clearCheckedChannels();
		groupChannelsManager.clearChannelCheckboxes();
		if (!groupChannelsManager.getChannels().isEmpty()) {
			groupChannelsManager.turnScrollerPage(groupChannelsManager
					.getChannels().get(0));
		}
	}

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		groupChannelsManager.clearCheckedChannels();
		groupChannelsManager.clearChannelCheckboxes();
	}

}

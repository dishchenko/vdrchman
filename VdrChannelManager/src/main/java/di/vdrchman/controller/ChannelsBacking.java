package di.vdrchman.controller;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.ChannelsManager;
import di.vdrchman.model.Channel;

@Model
public class ChannelsBacking {

	@Inject
	private ChannelsManager channelsManager;

	// The user is going to add a new Channel on top of the current channel list
	public void intendAddChannelOnTop() {
		Channel channel;

		channel = new Channel();
		channel.setTranspId(channelsManager.getFilteredTranspId());
		channelsManager.setEditedChannel(channel);
		channelsManager.setEditedChannelSeqno(channelsManager
				.calculateOnTopSeqno());
	}

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
	}

}

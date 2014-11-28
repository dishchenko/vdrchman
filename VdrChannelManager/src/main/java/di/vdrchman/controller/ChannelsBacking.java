package di.vdrchman.controller;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.ChannelsManager;

@Model
public class ChannelsBacking {

	@Inject
	private ChannelsManager channelsManager;

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
	}

}

package di.vdrchman.controller;

import javax.enterprise.inject.Model;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.ChannelRepository;
import di.vdrchman.data.ChannelsManager;
import di.vdrchman.data.TransponderRepository;
import di.vdrchman.model.Channel;

@Model
public class ChannelsBacking {

	@Inject
	private ChannelsManager channelsManager;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private ChannelRepository channelRepository;

	// The user is going to add a new channel on top of the current channel list
	public void intendAddChannelOnTop() {
		Channel channel;

		channel = new Channel();
		channel.setTranspId(channelsManager.getFilteredTranspId());
		channelsManager.setEditedChannel(channel);
		channelsManager.setEditedChannelSeqno(channelsManager
				.calculateOnTopSeqno());
	}

	// The user is going to add a new channel and place it right after
	// the checked channel in the list
	public void intendAddChannelAfter() {
		Channel channel;

		channel = new Channel();
		channel.setTranspId(channelsManager.getFilteredTranspId());
		channelsManager.collectCheckedChannels(
				channelsManager.getFilteredSourceId(),
				channelsManager.getFilteredTranspId());
		channelsManager.setEditedChannel(channel);
		channelsManager.setEditedChannelSeqno(channelRepository
				.getSeqno(channelsManager.getCheckedChannels().get(0)) + 1);
	}

	// Going to remove some checked channels
	public void intendRemoveChannels() {
		channelsManager.collectCheckedChannels(
				channelsManager.getFilteredSourceId(),
				channelsManager.getFilteredTranspId());
	}

	// Let's take the checked channel's data on the "clipboard"
	public void takeChannel() {
		channelsManager.collectCheckedChannels(
				channelsManager.getFilteredSourceId(),
				channelsManager.getFilteredTranspId());
		channelsManager.setTakenChannel(new Channel(channelsManager
				.getCheckedChannels().get(0)));
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
	}

	// The user's gonna add a new channel on top of the list
	// using data from the "clipboard"
	public void intendCopyChannelOnTop() {
		channelsManager.setEditedChannel(new Channel(channelsManager
				.getTakenChannel()));
		channelsManager.getEditedChannel().setId(null);
		channelsManager.setEditedChannelSeqno(channelsManager
				.calculateOnTopSeqno());
	}

	// The user is going to add a new channel using data from the
	// "clipboard" and place it right after the checked channel in the list
	public void intendCopyChannelAfter() {
		channelsManager.collectCheckedChannels(
				channelsManager.getFilteredSourceId(),
				channelsManager.getFilteredTranspId());
		channelsManager.setEditedChannel(new Channel(channelsManager
				.getTakenChannel()));
		channelsManager.getEditedChannel().setId(null);
		channelsManager.setEditedChannelSeqno(channelRepository
				.getSeqno(channelsManager.getCheckedChannels().get(0)) + 1);
	}

	// Remember the channel taken on the "clipboard"?
	// Move the original one on top of the current list
	public void moveChannelOnTop() {
		channelRepository.move(channelsManager.getTakenChannel(),
				channelsManager.calculateOnTopSeqno());
		channelsManager.retrieveAllChannels();
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
		channelsManager.turnScrollerPage(channelsManager.getTakenChannel());
	}

	// Now move the channel taken on the "clipboard" right after
	// the checked channel in the list
	public void moveChannelAfter() {
		int curSeqno;
		int newSeqno;

		channelsManager.collectCheckedChannels(
				channelsManager.getFilteredSourceId(),
				channelsManager.getFilteredTranspId());
		curSeqno = channelRepository
				.getSeqno(channelsManager.getTakenChannel());
		newSeqno = channelRepository.getSeqno(channelsManager
				.getCheckedChannels().get(0));
		if (newSeqno < curSeqno) {
			++newSeqno;
		}
		channelRepository.move(channelsManager.getTakenChannel(), newSeqno);
		channelsManager.retrieveAllChannels();
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
		channelsManager.turnScrollerPage(channelsManager.getTakenChannel());
	}

	// On changing the source filter selection clear the "clipboard"
	// if a source is selected and it is not the one taken channel
	// relates to. Clear the transponder filter selection if a source is
	// selected and it is not the one selected transponder relates to.
	// Also try to stay on the scroller page which includes the previous shown
	// page top channel
	public void onSourceMenuSelection(ValueChangeEvent event) {
		Channel lastPageTopChannel;
		long filteredSourceId;
		Channel takenChannel;
		long filteredTranspId;

		lastPageTopChannel = null;
		if (!channelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = channelsManager.getChannels().get(
					(channelsManager.getScrollerPage() - 1)
							* channelsManager.getRowsPerPage());
		}
		filteredSourceId = (Long) event.getNewValue();
		if (filteredSourceId >= 0) {
			takenChannel = channelsManager.getTakenChannel();
			if (takenChannel != null) {
				if (filteredSourceId != transponderRepository.findById(
						takenChannel.getTranspId()).getSourceId()) {
					channelsManager.setTakenChannel(null);
				}
			}
			filteredTranspId = channelsManager.getFilteredTranspId();
			if (filteredTranspId >= 0) {
				if (filteredSourceId != transponderRepository.findById(
						filteredTranspId).getSourceId()) {
					channelsManager.setFilteredTranspId(-1);
				}
			}
		}
		channelsManager.setFilteredSourceId(filteredSourceId);
		channelsManager.retrieveAllChannels();
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
		if (!channelsManager.getChannels().isEmpty()) {
			if (lastPageTopChannel != null) {
				if (filteredSourceId == transponderRepository.findById(
						lastPageTopChannel.getTranspId()).getSourceId()) {
					channelsManager.turnScrollerPage(lastPageTopChannel);
				} else {
					if (filteredSourceId < 0) {
						channelsManager.turnScrollerPage(lastPageTopChannel);
					} else {
						channelsManager.turnScrollerPage(channelsManager
								.getChannels().get(0));
					}
				}
			} else {
				channelsManager.turnScrollerPage(channelsManager.getChannels()
						.get(0));
			}
		}
	}

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
	}

}

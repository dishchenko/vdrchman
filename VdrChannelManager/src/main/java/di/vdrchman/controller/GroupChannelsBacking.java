package di.vdrchman.controller;

import javax.enterprise.inject.Model;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.ChannelRepository;
import di.vdrchman.data.GroupChannelsManager;
import di.vdrchman.data.TransponderRepository;
import di.vdrchman.model.Channel;

@Model
public class GroupChannelsBacking {

	@Inject
	private GroupChannelsManager groupChannelsManager;

	@Inject
	private TransponderRepository transponderRepository;

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
		groupChannelsManager.adjustLastScrollerPage();
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
		curSeqno = channelRepository.findSeqno(groupChannelsManager
				.getTakenChannel().getId(), groupChannelsManager
				.getShownGroupId());
		newSeqno = channelRepository.findSeqno(groupChannelsManager
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

	// On changing the source filter selection clear the transponder filter if a
	// source is selected and it is not the one selected transponder relates to.
	// Also try to stay on the scroller page which includes the previous shown
	// page top channel
	public void onSourceMenuSelection(ValueChangeEvent event) {
		Channel lastPageTopChannel;
		long filteredSourceId;

		lastPageTopChannel = null;
		if (!groupChannelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = groupChannelsManager.getChannels().get(
					(groupChannelsManager.getScrollerPage() - 1)
							* groupChannelsManager.getRowsPerPage());
		}
		filteredSourceId = (Long) event.getNewValue();
		groupChannelsManager.setFilteredSourceId(filteredSourceId);
		groupChannelsManager.setFilteredTranspId(-1);
		groupChannelsManager.retrieveAllChannels();
		groupChannelsManager.retrieveOrClearFilteredSourceTransponders();
		groupChannelsManager.clearCheckedChannels();
		groupChannelsManager.clearChannelCheckboxes();
		if (!groupChannelsManager.getChannels().isEmpty()) {
			if (lastPageTopChannel != null) {
				if (filteredSourceId == transponderRepository.findById(
						lastPageTopChannel.getTranspId()).getSourceId()) {
					groupChannelsManager.turnScrollerPage(lastPageTopChannel);
				} else {
					if (filteredSourceId < 0) {
						groupChannelsManager
								.turnScrollerPage(lastPageTopChannel);
					} else {
						groupChannelsManager
								.turnScrollerPage(groupChannelsManager
										.getChannels().get(0));
					}
				}
			} else {
				groupChannelsManager.turnScrollerPage(groupChannelsManager
						.getChannels().get(0));
			}
		}
	}

	// On changing the transponder filter selection try to stay on the scroller
	// page which includes the previous shown page top channel
	public void onTransponderMenuSelection(ValueChangeEvent event) {
		Channel lastPageTopChannel;
		long filteredTranspId;

		lastPageTopChannel = null;
		if (!groupChannelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = groupChannelsManager.getChannels().get(
					(groupChannelsManager.getScrollerPage() - 1)
							* groupChannelsManager.getRowsPerPage());
		}
		filteredTranspId = (Long) event.getNewValue();
		groupChannelsManager.setFilteredTranspId(filteredTranspId);
		groupChannelsManager.retrieveAllChannels();
		groupChannelsManager.clearCheckedChannels();
		groupChannelsManager.clearChannelCheckboxes();
		if (!groupChannelsManager.getChannels().isEmpty()) {
			if (lastPageTopChannel != null) {
				if (filteredTranspId == lastPageTopChannel.getTranspId()) {
					groupChannelsManager.turnScrollerPage(lastPageTopChannel);
				} else {
					if (filteredTranspId < 0) {
						groupChannelsManager
								.turnScrollerPage(lastPageTopChannel);
					} else {
						groupChannelsManager
								.turnScrollerPage(groupChannelsManager
										.getChannels().get(0));
					}
				}
			} else {
				groupChannelsManager.turnScrollerPage(groupChannelsManager
						.getChannels().get(0));
			}
		}
	}

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		groupChannelsManager.clearCheckedChannels();
		groupChannelsManager.clearChannelCheckboxes();
	}

}

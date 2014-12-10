package di.vdrchman.controller;

import java.util.List;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Model;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.ChannelRepository;
import di.vdrchman.data.ChannelsManager;
import di.vdrchman.data.SourceRepository;
import di.vdrchman.data.TransponderRepository;
import di.vdrchman.event.ChannelAction;
import di.vdrchman.model.Channel;
import di.vdrchman.model.Source;

@Model
public class ChannelsBacking {

	@Inject
	private ChannelsManager channelsManager;

	@Inject
	private SourceRepository sourceRepository;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private ChannelRepository channelRepository;

	@Inject
	private Event<ChannelAction> channelActionEvent;

	// The user is going to add a new channel on top of the current channel list
	public void intendAddChannelOnTop() {
		Channel channel;
		List<Source> sources;

		channel = new Channel();
		channel.setTranspId(channelsManager.getFilteredTranspId());
		channelsManager.setEditedChannel(channel);
		channelsManager.setEditedChannelSeqno(channelsManager
				.calculateOnTopSeqno());
		if (channelsManager.getFilteredSourceId() < 0) {
			sources = sourceRepository.findAll();
			if (!sources.isEmpty()) {
				channelsManager.setEditedSourceId(sources.get(0).getId());
			}
		} else {
			channelsManager.setEditedSourceId(channelsManager
					.getFilteredSourceId());
		}
		channelsManager.retrieveOrClearEditedSourceTransponders();
	}

	// The user is going to add a new channel and place it right after
	// the checked channel in the list
	public void intendAddChannelAfter() {
		Channel channel;
		List<Source> sources;

		channel = new Channel();
		channel.setTranspId(channelsManager.getFilteredTranspId());
		channelsManager.collectCheckedChannels();
		channelsManager.setEditedChannel(channel);
		channelsManager.setEditedChannelSeqno(channelRepository
				.findSeqno(channelsManager.getCheckedChannels().get(0)) + 1);
		if (channelsManager.getFilteredSourceId() < 0) {
			sources = sourceRepository.findAll();
			if (!sources.isEmpty()) {
				channelsManager.setEditedSourceId(sources.get(0).getId());
			}
		} else {
			channelsManager.setEditedSourceId(channelsManager
					.getFilteredSourceId());
		}
		channelsManager.retrieveOrClearEditedSourceTransponders();
	}

	// Really adding a new channel to the place specified
	public void doAddChannel() {
		channelRepository.add(channelsManager.getEditedChannel(),
				channelsManager.getEditedChannelSeqno());
		channelActionEvent.fire(new ChannelAction(channelsManager
				.getEditedChannel(), ChannelAction.Action.ADD));
		channelsManager.retrieveAllChannels();
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
		channelsManager.turnScrollerPage(channelsManager.getEditedChannel());
	}

	// The user is going to update a channel
	public void intendUpdateChannel(Channel channel) {
		channelsManager.setEditedChannel(new Channel(channel));
	}

	// Really updating the channel
	public void doUpdateChannel() {
		channelRepository.update(channelsManager.getEditedChannel());
		channelActionEvent.fire(new ChannelAction(channelsManager
				.getEditedChannel(), ChannelAction.Action.UPDATE));
		channelsManager.retrieveAllChannels();
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
	}

	// Going to remove some checked channels
	public void intendRemoveChannels() {
		channelsManager.collectCheckedChannels();
	}

	// Do that removal
	public void doRemoveChannels() {
		for (Channel channel : channelsManager.getCheckedChannels()) {
			channelRepository.delete(channel);
			channelActionEvent.fire(new ChannelAction(channel,
					ChannelAction.Action.DELETE));
		}
		channelsManager.retrieveAllChannels();
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
	}

	// Let's take the checked channel's data on the "clipboard"
	public void takeChannel() {
		channelsManager.collectCheckedChannels();
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
		channelsManager
				.setEditedSourceId(transponderRepository.findById(
						channelsManager.getEditedChannel().getTranspId())
						.getSourceId());
		channelsManager.retrieveOrClearEditedSourceTransponders();
	}

	// The user is going to add a new channel using data from the
	// "clipboard" and place it right after the checked channel in the list
	public void intendCopyChannelAfter() {
		channelsManager.collectCheckedChannels();
		channelsManager.setEditedChannel(new Channel(channelsManager
				.getTakenChannel()));
		channelsManager.getEditedChannel().setId(null);
		channelsManager.setEditedChannelSeqno(channelRepository
				.findSeqno(channelsManager.getCheckedChannels().get(0)) + 1);
		channelsManager
				.setEditedSourceId(transponderRepository.findById(
						channelsManager.getEditedChannel().getTranspId())
						.getSourceId());
		channelsManager.retrieveOrClearEditedSourceTransponders();
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

		channelsManager.collectCheckedChannels();
		curSeqno = channelRepository
				.findSeqno(channelsManager.getTakenChannel());
		newSeqno = channelRepository.findSeqno(channelsManager
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

	// The user is up to change the set of channel's groups
	public void intendUpdateGroups(Channel channel) {
		channelsManager.setEditedChannel(new Channel(channel));
		channelsManager.setUpdatedGroups(channelRepository.findGroups(channel
				.getId()));
		channelsManager.collectChannelGroupCheckboxes();
	}

	// Channel's group set change confirmed
	public void doUpdateGroups() {
		channelsManager.collectCheckedChannelGroups();
		channelRepository.updateGroups(channelsManager.getEditedChannel()
				.getId(), channelsManager.getCheckedChannelGroups());
		channelActionEvent.fire(new ChannelAction(channelsManager
				.getEditedChannel(), ChannelAction.Action.UPDATE_GROUPS));
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
		}
		channelsManager.setFilteredSourceId(filteredSourceId);
		channelsManager.setFilteredTranspId(-1);
		channelsManager.retrieveAllChannels();
		channelsManager.retrieveOrClearFilteredSourceTransponders();
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

	// On changing the transponder filter selection clear the "clipboard"
	// if a transponder is selected and it is not the one taken channel
	// relates to. Also try to stay on the scroller page which includes the
	// previous shown page top channel
	public void onTransponderMenuSelection(ValueChangeEvent event) {
		Channel lastPageTopChannel;
		long filteredTranspId;
		Channel takenChannel;

		lastPageTopChannel = null;
		if (!channelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = channelsManager.getChannels().get(
					(channelsManager.getScrollerPage() - 1)
							* channelsManager.getRowsPerPage());
		}
		filteredTranspId = (Long) event.getNewValue();
		if (filteredTranspId >= 0) {
			takenChannel = channelsManager.getTakenChannel();
			if (takenChannel != null) {
				if (filteredTranspId != takenChannel.getTranspId()) {
					channelsManager.setTakenChannel(null);
				}
			}
		}
		channelsManager.setFilteredTranspId(filteredTranspId);
		channelsManager.retrieveAllChannels();
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
		if (!channelsManager.getChannels().isEmpty()) {
			if (lastPageTopChannel != null) {
				if (filteredTranspId == lastPageTopChannel.getTranspId()) {
					channelsManager.turnScrollerPage(lastPageTopChannel);
				} else {
					if (filteredTranspId < 0) {
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

	// On changing the selected source in the menu of channel addition panel
	// reload content of that panel's transponders menu
	public void onEditedSourceMenuSelection(AjaxBehaviorEvent event) {
		channelsManager.retrieveOrClearEditedSourceTransponders();
	}

}

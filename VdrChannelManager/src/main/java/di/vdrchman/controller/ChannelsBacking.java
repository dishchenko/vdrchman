package di.vdrchman.controller;

import static di.vdrchman.util.Tools.*;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Model;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.ChannelRepository;
import di.vdrchman.data.ChannelsManager;
import di.vdrchman.data.GroupRepository;
import di.vdrchman.data.ScannedChannelRepository;
import di.vdrchman.data.SourceRepository;
import di.vdrchman.data.TransponderRepository;
import di.vdrchman.event.ChannelAction;
import di.vdrchman.model.Biss;
import di.vdrchman.model.Channel;
import di.vdrchman.model.Group;
import di.vdrchman.model.ScannedChannel;
import di.vdrchman.model.Source;
import di.vdrchman.model.Transponder;

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
	private ScannedChannelRepository scannedChannelRepository;

	@Inject
	private GroupRepository groupRepository;

	@Inject
	private Event<ChannelAction> channelActionEvent;

	// The user is going to add a new channel on top of the current channel list
	public void intendAddChannelOnTop() {
		Channel channel;
		List<Source> sources;
		List<Group> groups;

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
		groups = groupRepository.findAll();
		if (groups.size() == 1) {
			channelsManager.setAddedChannelGroupId(groups.get(0).getId());
		} else {
			channelsManager.setAddedChannelGroupId(-1);
		}
	}

	// The user is going to add a new channel and place it right after
	// the checked channel in the list
	public void intendAddChannelAfter() {
		Channel channel;
		List<Source> sources;
		List<Group> groups;

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
		groups = groupRepository.findAll();
		if (groups.size() == 1) {
			channelsManager.setAddedChannelGroupId(groups.get(0).getId());
		} else {
			channelsManager.setAddedChannelGroupId(-1);
		}
	}

	// Really adding a new channel to the place specified
	public void doAddChannel() {
		Channel editedChannel;
		long channelGroupId;
		List<Group> channelGroups;

		editedChannel = channelsManager.getEditedChannel();
		channelRepository.add(editedChannel,
				channelsManager.getEditedChannelSeqno());
		channelActionEvent.fire(new ChannelAction(editedChannel,
				ChannelAction.Action.ADD));
		channelGroupId = channelsManager.getAddedChannelGroupId();
		if (channelGroupId != -1) {
			channelGroups = new ArrayList<Group>();
			channelGroups.add(groupRepository.findById(channelGroupId));
			channelRepository
					.updateGroups(editedChannel.getId(), channelGroups);
			channelActionEvent.fire(new ChannelAction(editedChannel,
					ChannelAction.Action.UPDATE_GROUPS));
		}
		channelsManager.retrieveAllChannels();
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
		channelsManager.turnScrollerPage(editedChannel);
	}

	// The user is going to update a channel
	public void intendUpdateChannel(Channel channel) {
		Transponder transponder;

		channelsManager.setEditedChannel(new Channel(channel));
		if (channelsManager.getComparisonFilter() == COMPARISON_CHANGED_MAIN) {
			transponder = transponderRepository.findById(channel.getTranspId());
			channelsManager.setComparedScannedChannel(scannedChannelRepository
					.findBySourceFrequencyPolarizationStreamSidApid(
							sourceRepository
									.findById(transponder.getSourceId())
									.getName(), transponder.getFrequency(),
							transponder.getPolarization(), transponder
									.getStreamId(), channel.getSid(), channel
									.getApid()));
		} else {
			channelsManager.setComparedScannedChannel(null);
		}
	}

	// Really updating the channel
	public void doUpdateChannel() {
		Channel editedChannel;
		ScannedChannel comparedScannedChannel;

		editedChannel = channelsManager.getEditedChannel();
		if (channelsManager.getComparisonFilter() == COMPARISON_CHANGED_MAIN) {
			comparedScannedChannel = channelsManager
					.getComparedScannedChannel();
			editedChannel.setPcr(comparedScannedChannel.getPcr());
			editedChannel.setRid(comparedScannedChannel.getRid());
			editedChannel.setScannedName(comparedScannedChannel
					.getScannedName());
			editedChannel.setProviderName(comparedScannedChannel
					.getProviderName());
		}
		channelRepository.update(editedChannel);
		channelActionEvent.fire(new ChannelAction(editedChannel,
				ChannelAction.Action.UPDATE));
		channelsManager.retrieveAllChannels();
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
		if (channelsManager.getComparisonFilter() != COMPARISON_NONE) {
			channelsManager.adjustLastScrollerPage();
		}
	}

	// The user is going to update channel BISS keys
	public void intendUpdateBissKeys(Channel channel) {
		Biss biss;

		biss = channelRepository.findBissKeys(channel);

		if (biss == null) {
			biss = new Biss();
			biss.setChannelId(channel.getId());
		} else {
			biss = new Biss(biss);
		}

		channelsManager.setEditedBiss(biss);
	}

	// BISS keys update confirmed
	public void doUpdateBissKeys() {
		channelRepository.addOrUpdateBissKeys(channelsManager.getEditedBiss());
	}

	// BISS keys removal
	public void doRemoveBissKeys() {
		channelRepository.removeBissKeys(channelsManager.getEditedBiss());
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
		channelsManager.adjustLastScrollerPage();
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
		List<Group> groups;

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
		groups = groupRepository.findAll();
		if (groups.size() == 1) {
			channelsManager.setAddedChannelGroupId(groups.get(0).getId());
		} else {
			channelsManager.setAddedChannelGroupId(-1);
		}
	}

	// The user is going to add a new channel using data from the
	// "clipboard" and place it right after the checked channel in the list
	public void intendCopyChannelAfter() {
		List<Group> groups;

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
		groups = groupRepository.findAll();
		if (groups.size() == 1) {
			channelsManager.setAddedChannelGroupId(groups.get(0).getId());
		} else {
			channelsManager.setAddedChannelGroupId(-1);
		}
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
		curSeqno = channelRepository.findSeqno(channelsManager
				.getTakenChannel());
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
		Channel editedChannel;

		channelsManager.collectCheckedChannelGroups();
		editedChannel = channelsManager.getEditedChannel();
		channelRepository.updateGroups(editedChannel.getId(),
				channelsManager.getCheckedChannelGroups());
		channelActionEvent.fire(new ChannelAction(editedChannel,
				ChannelAction.Action.UPDATE_GROUPS));
	}

	// The user have chosen to sort channels
	public void doSort() {
		channelRepository.sort(channelsManager.getSortMode());
		channelsManager.retrieveAllChannels();
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
		channelsManager.setScrollerPage(1);
	}

	// On changing the source filter selection clear the transponder filter if a
	// source is selected and it is not the one selected transponder relates to.
	// Also try to stay on the scroller page which includes the previous shown
	// page top channel
	public void onSourceMenuSelection(ValueChangeEvent event) {
		Channel lastPageTopChannel;
		long filteredSourceId;

		lastPageTopChannel = null;
		if (!channelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = channelsManager.getChannels().get(
					(channelsManager.getScrollerPage() - 1)
							* channelsManager.getRowsPerPage());
		}
		filteredSourceId = (Long) event.getNewValue();
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

	// On changing the transponder filter selection try to stay on the scroller
	// page which includes the previous shown page top channel
	public void onTransponderMenuSelection(ValueChangeEvent event) {
		Channel lastPageTopChannel;
		long filteredTranspId;

		lastPageTopChannel = null;
		if (!channelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = channelsManager.getChannels().get(
					(channelsManager.getScrollerPage() - 1)
							* channelsManager.getRowsPerPage());
		}
		filteredTranspId = (Long) event.getNewValue();
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

	// On changing the comparison result filter selection try to stay
	// on the scroller page which includes the previous shown page top channel
	public void onComparisonMenuSelection(ValueChangeEvent event) {
		Channel lastPageTopChannel;
		int comparisonFilter;

		lastPageTopChannel = null;
		if (!channelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = channelsManager.getChannels().get(
					(channelsManager.getScrollerPage() - 1)
							* channelsManager.getRowsPerPage());
		}
		comparisonFilter = (Integer) event.getNewValue();
		channelsManager.setComparisonFilter(comparisonFilter);
		channelsManager.retrieveAllChannels();
		channelsManager.clearCheckedChannels();
		channelsManager.clearChannelCheckboxes();
		if (!channelsManager.getChannels().isEmpty()) {
			if (lastPageTopChannel != null) {
				if (!channelsManager.turnScrollerPage(lastPageTopChannel)) {
					channelsManager.turnScrollerPage(channelsManager
							.getChannels().get(0));
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

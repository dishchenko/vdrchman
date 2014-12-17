package di.vdrchman.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import javax.inject.Named;

import di.vdrchman.event.ChannelAction;
import di.vdrchman.event.SourceAction;
import di.vdrchman.event.TransponderAction;
import di.vdrchman.model.Channel;
import di.vdrchman.model.Group;
import di.vdrchman.model.Transponder;

@SessionScoped
@Named
public class GroupChannelsManager implements Serializable {

	private static final long serialVersionUID = -8886902714319782369L;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private ChannelRepository channelRepository;

	@Inject
	private GroupRepository groupRepository;

	// The ID of the group which channels are shown in the table
	private long shownGroupId = -1;

	// Group channel list for the current application user
	private List<Channel> channels;

	// Indicates that channels list refresh is suggested
	private boolean channelsRefreshNeeded = false;

	// Number of table rows per page
	private final int rowsPerPage = 15;
	// Current table scroller page
	private int scrollerPage = 1;

	// Map of channel IDs and checked checkboxes
	private Map<Long, Boolean> channelCheckboxes = new HashMap<Long, Boolean>();
	// List of checked channels built on checkboxes map
	private List<Channel> checkedChannels = new ArrayList<Channel>();

	// The "clipboard": the place to store the channel taken by user
	private Channel takenChannel = null;

	// Fill in checkedChannels list with channels corresponding
	// to checkboxes checked in the data table on the page
	public void collectCheckedChannels() {
		clearCheckedChannels();

		for (Channel channel : channels) {
			if (channelCheckboxes.get(channel.getId()) != null) {
				if (channelCheckboxes.get(channel.getId())) {
					checkedChannels.add(channel);
				}
			}
		}
	}

	// Clear the list of checked channels
	public void clearCheckedChannels() {
		checkedChannels.clear();
	}

	// Clear the map of channel checkboxes
	public void clearChannelCheckboxes() {
		channelCheckboxes.clear();
	}

	// Find and set the table scroller page to show the channel given
	public void turnScrollerPage(Channel channel) {
		scrollerPage = (channelRepository.findSeqno(channel.getId(),
				shownGroupId) - 1) / rowsPerPage + 1;
	}

	// Calculate the sequence number for the channel to be placed on top
	// of the current channel list. If current channel list is not empty
	// then it is the sequence number of its top channel. Otherwise it is 1
	public int calculateOnTopSeqno() {
		int result;

		if (channels.isEmpty()) {
			result = 1;
		} else {
			result = channelRepository.findSeqno(channels.get(0).getId(),
					shownGroupId);
		}

		return result;
	}

	// Cleanup the GroupChannelManager's data on SourceAction if needed
	public void onSourceAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final SourceAction sourceAction) {
		Transponder transponder;

		if (sourceAction.getAction() == SourceAction.Action.DELETE) {
			channelsRefreshNeeded = true;

			if (takenChannel != null) {
				transponder = transponderRepository.findById(takenChannel
						.getId());
				if (transponder != null) {
					if (sourceAction.getSource().getId()
							.equals(transponder.getSourceId())) {
						takenChannel = null;
					}
				} else {
					takenChannel = null;
				}
			}
		}
	}

	// Cleanup the GroupChannelManager's data on TransponderAction if needed
	public void onTransponderAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final TransponderAction transponderAction) {
		if (transponderAction.getAction() == TransponderAction.Action.DELETE) {
			channelsRefreshNeeded = true;

			if (takenChannel != null) {
				if (transponderAction.getTransponder().getId()
						.equals(takenChannel.getTranspId())) {
					takenChannel = null;
				}
			}
		}
	}

	// Cleanup the GroupChannelManager's data on ChannelAction if needed
	public void onChannelAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final ChannelAction channelAction) {
		if (channelAction.getAction() == ChannelAction.Action.DELETE) {
			channelsRefreshNeeded = true;

			if (takenChannel != null) {
				if (channelAction.getChannel().getId()
						.equals(takenChannel.getId())) {
					takenChannel = null;
				}
			}
		}

		if ((channelAction.getAction() == ChannelAction.Action.UPDATE)
				|| (channelAction.getAction() == ChannelAction.Action.UPDATE_GROUPS)) {
			channelsRefreshNeeded = true;
		}

	}

	// Re(Fill) in the channel list only if it is suggested. Also try to
	// turn the table scroller page to keep the last page top channel shown
	public void refreshChannelsIfNeeded() {
		Channel lastPageTopChannel;

		if (channelsRefreshNeeded) {
			lastPageTopChannel = null;
			if (!channels.isEmpty()) {
				lastPageTopChannel = channels.get((scrollerPage - 1)
						* rowsPerPage);
			}

			retrieveAllChannels();

			if (!channels.isEmpty()) {
				if (lastPageTopChannel != null) {
					if (channelRepository.findById(lastPageTopChannel.getId()) != null) {
						turnScrollerPage(lastPageTopChannel);
					} else {
						scrollerPage = 1;
					}
				} else {
					scrollerPage = 1;
				}
			}

			channelsRefreshNeeded = false;
		}
	}

	// (Re)Fill in the channel list
	@PostConstruct
	public void retrieveAllChannels() {
		List<Group> groups;

		if (shownGroupId < 0) {
			groups = groupRepository.findAll();
			if (!groups.isEmpty()) {
				shownGroupId = groups.get(0).getId();
			}
		}

		channels = channelRepository.findAllInGroup(shownGroupId);
	}

	public long getShownGroupId() {

		return shownGroupId;
	}

	public void setShownGroupId(long shownGroupId) {
		this.shownGroupId = shownGroupId;
	}

	public List<Channel> getChannels() {

		return channels;
	}

	public int getRowsPerPage() {

		return rowsPerPage;
	}

	public int getScrollerPage() {

		return scrollerPage;
	}

	public void setScrollerPage(int scrollerPage) {
		this.scrollerPage = scrollerPage;
	}

	public Map<Long, Boolean> getChannelCheckboxes() {

		return channelCheckboxes;
	}

	public List<Channel> getCheckedChannels() {

		return checkedChannels;
	}

	public Channel getTakenChannel() {

		return takenChannel;
	}

	public void setTakenChannel(Channel takenChannel) {
		this.takenChannel = takenChannel;
	}

}

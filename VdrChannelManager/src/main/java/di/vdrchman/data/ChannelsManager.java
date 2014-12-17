package di.vdrchman.data;

import static di.vdrchman.util.Tools.*;

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

import di.vdrchman.event.ScannedChannelAction;
import di.vdrchman.event.SourceAction;
import di.vdrchman.event.TransponderAction;
import di.vdrchman.model.Channel;
import di.vdrchman.model.Group;
import di.vdrchman.model.Source;
import di.vdrchman.model.Transponder;

@SessionScoped
@Named
public class ChannelsManager implements Serializable {

	private static final long serialVersionUID = -1754936915825346390L;

	@Inject
	private SourceRepository sourceRepository;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private ChannelRepository channelRepository;

	@Inject
	private GroupRepository groupRepository;

	// ID of the source to filter channel list on.
	// No filtering if it's negative.
	private long filteredSourceId = -1;
	// ID of the transponder to filter channel list on.
	// No filtering if it's negative.
	private long filteredTranspId = -1;

	// List of filtered source transponders
	private List<Transponder> filteredSourceTransponders = new ArrayList<Transponder>();

	// Indicates that filtered source transponder list refresh is suggested
	private boolean filteredSourceTranspondersRefreshNeeded = false;

	// (Filtered) Channel list for the current application user
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

	// The channel which the user is going to add/update
	private Channel editedChannel = new Channel();

	// Sequence number of the edited channel
	private int editedChannelSeqno;

	// ID of the source the new channel relates to
	private long editedSourceId = -1;

	// List of edited source transponders
	private List<Transponder> editedSourceTransponders = new ArrayList<Transponder>();

	// The "clipboard": the place to store the channel taken by user
	private Channel takenChannel = null;

	// The list of groups the edited channel is a member of
	private List<Group> updatedGroups;

	// Map of channel group IDs and checked checkboxes
	private Map<Long, Boolean> channelGroupCheckboxes = new HashMap<Long, Boolean>();
	// List of checked channel groups built on checkboxes map
	private List<Group> checkedChannelGroups = new ArrayList<Group>();

	// Comparison result filter value. See Tools.COMPARISON_*
	private int comparisonFilter = 0;

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

	// Find and set the table scroller page to show the channel given.
	// Return true if the page with the channel is found, false otherwise
	public boolean turnScrollerPage(Channel channel) {
		boolean result;
		List<Channel> channels;
		int i;

		result = false;

		if ((filteredSourceId >= 0) || (filteredTranspId >= 0)) {
			channels = channelRepository.findAll(filteredSourceId,
					filteredTranspId);
			i = 0;
			for (Channel theChannel : channels) {
				if (theChannel.getId().equals(channel.getId())) {
					scrollerPage = i / rowsPerPage + 1;
					result = true;
					break;
				}
				++i;
			}
		} else {
			scrollerPage = (channelRepository.findSeqno(channel) - 1)
					/ rowsPerPage + 1;
			result = true;
		}

		return result;
	}

	// Calculate the sequence number for the channel to be placed on top
	// of the current channel list. If current channel list is not empty
	// then it is the sequence number of its top channel. Otherwise it is
	// calculated the way the channel to be placed right after
	// the last existing channel of previous transponders
	public int calculateOnTopSeqno() {
		int result;
		Transponder transponder;
		Integer maxChannelSeqnoWithinTransponder;
		Source source;
		Integer maxTransponderSeqnoWithinSource;

		if (channels.isEmpty()) {
			result = 1;
			if (filteredTranspId >= 0) {
				transponder = transponderRepository
						.findPrevious(filteredTranspId);
				while (transponder != null) {
					maxChannelSeqnoWithinTransponder = channelRepository
							.findMaxSeqno(transponder.getId());
					if (maxChannelSeqnoWithinTransponder != null) {
						result = maxChannelSeqnoWithinTransponder + 1;
						break;
					}
					transponder = transponderRepository
							.findPrevious(transponder);
				}
			} else {
				if (filteredSourceId >= 0) {
					source = sourceRepository.findPrevious(filteredSourceId);
					while (source != null) {
						maxTransponderSeqnoWithinSource = transponderRepository
								.findMaxSeqno(source.getId());
						if (maxTransponderSeqnoWithinSource != null) {
							maxChannelSeqnoWithinTransponder = channelRepository
									.findMaxSeqno(transponderRepository
											.findBySeqno(
													maxTransponderSeqnoWithinSource)
											.getId());
							if (maxChannelSeqnoWithinTransponder != null) {
								result = maxChannelSeqnoWithinTransponder + 1;
								break;
							}
						}
						source = sourceRepository.findPrevious(source.getId());
					}
				}
			}
		} else {
			result = channelRepository.findSeqno(channels.get(0));
		}

		return result;
	}

	// Cleanup the ChannelManager's data on SourceAction if needed
	public void onSourceAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final SourceAction sourceAction) {
		long sourceId;
		Transponder transponder;

		if (sourceAction.getAction() == SourceAction.Action.DELETE) {
			sourceId = sourceAction.getSource().getId();

			if (sourceId == filteredSourceId) {
				filteredSourceId = -1;
				filteredSourceTranspondersRefreshNeeded = true;
			}
			if (filteredSourceId < 0) {
				channelsRefreshNeeded = true;
			}

			if (takenChannel != null) {
				transponder = transponderRepository.findById(takenChannel
						.getId());
				if (transponder != null) {
					if (sourceId == transponder.getSourceId()) {
						takenChannel = null;
					}
				} else {
					takenChannel = null;
				}
			}
		}
	}

	// Cleanup the ChannelManager's data on TransponderAction if needed
	public void onTransponderAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final TransponderAction transponderAction) {
		long transpId;

		if (transponderAction.getTransponder().getSourceId() == filteredSourceId) {
			filteredSourceTranspondersRefreshNeeded = true;
		}

		if (transponderAction.getAction() == TransponderAction.Action.DELETE) {
			transpId = transponderAction.getTransponder().getId();

			if (transpId == filteredTranspId) {
				filteredTranspId = -1;
			}
			if (filteredTranspId < 0) {
				channelsRefreshNeeded = true;
			}

			if (takenChannel != null) {
				if (transpId == takenChannel.getTranspId()) {
					takenChannel = null;
				}
			}
		}
	}

	// Cleanup the ChannelManager's data on ScannedChannelAction if needed
	public void onScannedChannelAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final ScannedChannelAction scannedChannelAction) {
		if (scannedChannelAction.getAction() == ScannedChannelAction.Action.SCAN_PROCESSED) {
			if (comparisonFilter != COMPARISON_NONE) {
				channelsRefreshNeeded = true;
			}
		}
	}

	// Re(Fill) in the filtered source transponder list only if it is suggested
	public void refreshFilteredSourceTranspondersIfNeeded() {
		if (filteredSourceTranspondersRefreshNeeded) {
			retrieveOrClearFilteredSourceTransponders();

			filteredSourceTranspondersRefreshNeeded = false;
		}
	}

	// Re(Fill) in the filtered source transponder list. Clear the list
	// if there is no filtered source (negative source ID value)
	public void retrieveOrClearFilteredSourceTransponders() {
		if (filteredSourceId >= 0) {
			filteredSourceTransponders = transponderRepository
					.findAll(filteredSourceId);
		} else {
			filteredSourceTransponders.clear();
		}
	}

	// Re(Fill) in the edited source transponder list. Clear the list
	// if there is no edited source (negative source ID value)
	public void retrieveOrClearEditedSourceTransponders() {
		if (editedSourceId >= 0) {
			editedSourceTransponders = transponderRepository
					.findAll(editedSourceId);
		} else {
			editedSourceTransponders.clear();
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

	// Fill in channelGroupCheckboxes map with values indicating that
	// the channel is (not) a member of groups
	public void collectChannelGroupCheckboxes() {
		List<Group> groups;
		long groupId;

		groups = groupRepository.findAll();

		clearChannelGroupCheckboxes();

		for (Group group : groups) {
			groupId = group.getId();

			for (Group channelGroup : updatedGroups) {
				if (channelGroup.getId() == groupId) {
					channelGroupCheckboxes.put(groupId, true);
					break;
				}
			}
		}
	}

	// Fill in checkedChannelGroups list with channel groups corresponding
	// to checkboxes checked in the update channel groups dialog
	public void collectCheckedChannelGroups() {
		List<Group> groups;

		groups = groupRepository.findAll();

		clearCheckedChannelGroups();

		for (Group group : groups) {
			if (channelGroupCheckboxes.get(group.getId()) != null) {
				if (channelGroupCheckboxes.get(group.getId())) {
					checkedChannelGroups.add(group);
				}
			}
		}
	}

	// Clear the list of checked channel groups
	public void clearCheckedChannelGroups() {
		checkedChannelGroups.clear();
	}

	// Clear the map of channel group checkboxes
	public void clearChannelGroupCheckboxes() {
		channelGroupCheckboxes.clear();
	}

	// (Re)Fill in the channel list
	@PostConstruct
	public void retrieveAllChannels() {
		channels = channelRepository
				.findAll(filteredSourceId, filteredTranspId);
	}

	public long getFilteredSourceId() {

		return filteredSourceId;
	}

	public void setFilteredSourceId(long filteredSourceId) {
		this.filteredSourceId = filteredSourceId;
	}

	public long getFilteredTranspId() {

		return filteredTranspId;
	}

	public void setFilteredTranspId(long filteredTranspId) {
		this.filteredTranspId = filteredTranspId;
	}

	public List<Transponder> getFilteredSourceTransponders() {

		return filteredSourceTransponders;
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

	public Channel getEditedChannel() {

		return editedChannel;
	}

	public void setEditedChannel(Channel editedChannel) {
		this.editedChannel = editedChannel;
	}

	public int getEditedChannelSeqno() {

		return editedChannelSeqno;
	}

	public void setEditedChannelSeqno(int editedChannelSeqno) {
		this.editedChannelSeqno = editedChannelSeqno;
	}

	public long getEditedSourceId() {

		return editedSourceId;
	}

	public void setEditedSourceId(long editedSourceId) {
		this.editedSourceId = editedSourceId;
	}

	public List<Transponder> getEditedSourceTransponders() {

		return editedSourceTransponders;
	}

	public Channel getTakenChannel() {

		return takenChannel;
	}

	public void setTakenChannel(Channel takenChannel) {
		this.takenChannel = takenChannel;
	}

	public List<Group> getUpdatedGroups() {

		return updatedGroups;
	}

	public void setUpdatedGroups(List<Group> updatedGroups) {
		this.updatedGroups = updatedGroups;
	}

	public Map<Long, Boolean> getChannelGroupCheckboxes() {

		return channelGroupCheckboxes;
	}

	public List<Group> getCheckedChannelGroups() {

		return checkedChannelGroups;
	}

	public int getComparisonFilter() {

		return comparisonFilter;
	}

	public void setComparisonFilter(int comparisonFilter) {
		this.comparisonFilter = comparisonFilter;
	}

}

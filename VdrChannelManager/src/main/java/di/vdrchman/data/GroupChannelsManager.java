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
import di.vdrchman.event.ScannedChannelAction;
import di.vdrchman.event.SourceAction;
import di.vdrchman.event.TransponderAction;
import di.vdrchman.model.Channel;
import di.vdrchman.model.Group;
import di.vdrchman.model.Source;
import di.vdrchman.model.Transponder;

@SessionScoped
@Named
public class GroupChannelsManager implements Serializable {

	private static final long serialVersionUID = -8886902714319782369L;

	@Inject
	private SourceRepository sourceRepository;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private ChannelRepository channelRepository;

	@Inject
	private GroupRepository groupRepository;

	// The ID of the group which channels are shown in the table
	private long shownGroupId = -1;

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

	// (Filtered) Group channel list for the current application user
	private List<Channel> channels;

	// Indicates that channels list refresh is suggested
	private boolean channelsRefreshNeeded = false;

	// Number of table rows per page
	private final int rowsPerPage = 10;
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
		List<Channel> channels;
		int i;

		if ((filteredSourceId >= 0) || (filteredTranspId >= 0)) {
			channels = channelRepository.findAllInGroup(shownGroupId,
					filteredSourceId, filteredTranspId);
			i = 0;
			for (Channel theChannel : channels) {
				if (theChannel.getId().equals(channel.getId())) {
					scrollerPage = i / rowsPerPage + 1;
					break;
				}
				++i;
			}
		} else {
			scrollerPage = (channelRepository.findSeqno(channel.getId(),
					shownGroupId) - 1) / rowsPerPage + 1;
		}
	}

	// Set scroller page value to the last page if it is beyond now.
	public void adjustLastScrollerPage() {
		int maxPageNo;

		maxPageNo = (channels.size() - 1) / rowsPerPage + 1;
		if (scrollerPage > maxPageNo) {
			scrollerPage = maxPageNo;
		}
	}

	// Calculate the sequence number for the channel to be placed on top
	// of the current channel list. If current channel list is not empty
	// then it is the sequence number of its top channel. Otherwise it is
	// calculated the way the channel to be placed right after the last existing
	// channel of previous transponders
	public int calculateOnTopSeqno() {
		int result;
		Transponder transponder;
		Integer maxChannelSeqnoWithinTransponder;
		Source source;
		Integer maxTranspSeqnoWithinSource;

		if (channels.isEmpty()) {
			result = 1;
			if (filteredTranspId >= 0) {
				transponder = transponderRepository
						.findPrevious(filteredTranspId);
				while (transponder != null) {
					maxChannelSeqnoWithinTransponder = channelRepository
							.findMaxGroupSeqno(shownGroupId,
									transponder.getId());
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
						maxTranspSeqnoWithinSource = transponderRepository
								.findMaxSeqno(source.getId());
						if (maxTranspSeqnoWithinSource != null) {
							maxChannelSeqnoWithinTransponder = channelRepository
									.findMaxGroupSeqno(
											shownGroupId,
											transponderRepository.findBySeqno(
													maxTranspSeqnoWithinSource)
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
			result = channelRepository.findSeqno(channels.get(0).getId(),
					shownGroupId);
		}

		return result;
	}

	// Cleanup the GroupChannelsManager's data on SourceAction if needed
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

	// Cleanup the GroupChannelsManager's data on TransponderAction if needed
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

	// Cleanup the GroupChannelsManager's data on ChannelAction if needed
	public void onChannelAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final ChannelAction channelAction) {
		long transpId;
		long sourceId;

		if ((channelAction.getAction() == ChannelAction.Action.DELETE)
				|| (channelAction.getAction() == ChannelAction.Action.UPDATE)
				|| (channelAction.getAction() == ChannelAction.Action.UPDATE_GROUPS)) {
			transpId = channelAction.getChannel().getTranspId();
			if (filteredTranspId == transpId) {
				channelsRefreshNeeded = true;
			} else {
				if (filteredTranspId < 0) {
					if (filteredSourceId >= 0) {
						sourceId = transponderRepository.findById(transpId)
								.getSourceId();
						if (filteredSourceId == sourceId) {
							channelsRefreshNeeded = true;
						}
					} else {
						channelsRefreshNeeded = true;
					}
				}
			}

			if (channelAction.getAction() == ChannelAction.Action.DELETE) {
				if (takenChannel != null) {
					if (channelAction.getChannel().getId()
							.equals(takenChannel.getId())) {
						takenChannel = null;
					}
				}
			}
		}
	}

	// Cleanup the GroupChannelsManager's data on ScannedChannelAction if needed
	public void onScannedChannelAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final ScannedChannelAction scannedChannelAction) {
		if (scannedChannelAction.getAction() == ScannedChannelAction.Action.SCAN_PROCESSED) {
			if ((filteredSourceId == scannedChannelAction.getSourceId())
					|| (filteredSourceId < 0)) {
				channelsRefreshNeeded = true;
			}
		}

		if ((scannedChannelAction.getAction() == ScannedChannelAction.Action.CHANNEL_UPDATED)
				|| (scannedChannelAction.getAction() == ScannedChannelAction.Action.CHANNEL_REMOVED)) {
			if (filteredSourceId < 0) {
				channelsRefreshNeeded = true;
			} else {
				if (filteredSourceId == scannedChannelAction.getSourceId()) {
					if ((filteredTranspId == scannedChannelAction.getTranspId())
							|| (filteredTranspId < 0)) {
						channelsRefreshNeeded = true;
					}
				}
			}
		}

		if (channelsRefreshNeeded) {
			if (scannedChannelAction.getAction() == ScannedChannelAction.Action.CHANNEL_REMOVED) {
				takenChannel = null;
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

		channels = channelRepository.findAllInGroup(shownGroupId,
				filteredSourceId, filteredTranspId);
	}

	public long getShownGroupId() {

		return shownGroupId;
	}

	public void setShownGroupId(long shownGroupId) {
		this.shownGroupId = shownGroupId;
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

	public Channel getTakenChannel() {

		return takenChannel;
	}

	public void setTakenChannel(Channel takenChannel) {
		this.takenChannel = takenChannel;
	}

}

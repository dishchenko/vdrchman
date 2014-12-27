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
import di.vdrchman.model.IgnoredChannel;
import di.vdrchman.model.ScannedChannel;
import di.vdrchman.model.Transponder;

@SessionScoped
@Named
public class IgnoredChannelsManager implements Serializable {

	private static final long serialVersionUID = 8895904229100170983L;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private IgnoredChannelRepository ignoredChannelRepository;

	// ID of the source to filter ignored channel list on.
	// No filtering if it's negative.
	private long filteredSourceId = -1;
	// ID of the transponder to filter ignored channel list on.
	// No filtering if it's negative.
	private long filteredTranspId = -1;

	// List of filtered source transponders
	private List<Transponder> filteredSourceTransponders = new ArrayList<Transponder>();

	// Indicates that filtered source transponder list refresh is suggested
	private boolean filteredSourceTranspondersRefreshNeeded = false;

	// (Filtered) Ignored channel list for the current application user
	private List<IgnoredChannel> channels;

	// Indicates that channels list refresh is suggested
	private boolean channelsRefreshNeeded = false;

	// Number of table rows per page
	private final int rowsPerPage = 10;
	// Current table scroller page
	private int scrollerPage = 1;

	// Map of channel IDs and checked checkboxes
	private Map<Long, Boolean> channelCheckboxes = new HashMap<Long, Boolean>();
	// List of checked channels built on checkboxes map
	private List<IgnoredChannel> checkedChannels = new ArrayList<IgnoredChannel>();

	// Comparison result filter value. See Tools.COMPARISON_*
	private int comparisonFilter = 0;

	// The channel which the user is going to add/update
	private IgnoredChannel editedChannel = new IgnoredChannel();

	// The scanned channel corresponding to the edited channel
	private ScannedChannel comparedScannedChannel;

	// Fill in checkedChannels list with channels corresponding
	// to checkboxes checked in the data table on the page
	public void collectCheckedChannels() {
		clearCheckedChannels();

		for (IgnoredChannel channel : channels) {
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
	public boolean turnScrollerPage(IgnoredChannel channel) {
		boolean result;
		List<IgnoredChannel> channels;
		int i;

		result = false;

		channels = ignoredChannelRepository.findAll(filteredSourceId,
				filteredTranspId, comparisonFilter);
		i = 0;
		for (IgnoredChannel theChannel : channels) {
			if (theChannel.getId().equals(channel.getId())) {
				scrollerPage = i / rowsPerPage + 1;
				result = true;
				break;
			}
			++i;
		}

		return result;
	}

	// Set scroller page value to the last page if it is beyond now.
	public void adjustLastScrollerPage() {
		int maxPageNo;

		maxPageNo = (channels.size() - 1) / rowsPerPage + 1;
		if (scrollerPage > maxPageNo) {
			scrollerPage = maxPageNo;
		}
	}

	// Cleanup the IgnoredChannelsManager's data on SourceAction if needed
	public void onSourceAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final SourceAction sourceAction) {
		long sourceId;

		if (sourceAction.getAction() == SourceAction.Action.DELETE) {
			sourceId = sourceAction.getSource().getId();

			if (sourceId == filteredSourceId) {
				filteredSourceId = -1;
				filteredSourceTranspondersRefreshNeeded = true;
			}
			if (filteredSourceId < 0) {
				channelsRefreshNeeded = true;
			}
		}
	}

	// Cleanup the IgnoredChannelsManager's data on TransponderAction if needed
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
		}
	}

	// Cleanup the IgnoredChannelsManager's data on ScannedChannelAction if
	// needed
	public void onScannedChannelAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final ScannedChannelAction scannedChannelAction) {
		if (scannedChannelAction.getAction() == ScannedChannelAction.Action.SCAN_PROCESSED) {
			if ((filteredSourceId == scannedChannelAction.getSourceId())
					|| (filteredSourceId < 0)) {
				channelsRefreshNeeded = true;
			}
		}

		if (scannedChannelAction.getAction() == ScannedChannelAction.Action.IGNORED_CHANNEL_ADDED) {
			if (comparisonFilter == COMPARISON_NONE) {
				if (filteredSourceId < 0) {
					channelsRefreshNeeded = true;
				} else {
					if (filteredSourceId == scannedChannelAction.getSourceId()) {
						if ((filteredTranspId == scannedChannelAction
								.getTranspId()) || (filteredTranspId < 0)) {
							channelsRefreshNeeded = true;
						}
					}
				}
			}
		}

		if ((scannedChannelAction.getAction() == ScannedChannelAction.Action.IGNORED_CHANNEL_UPDATED)
				|| (scannedChannelAction.getAction() == ScannedChannelAction.Action.IGNORED_CHANNEL_REMOVED)) {
			if ((comparisonFilter == COMPARISON_NONE)
					|| (comparisonFilter == COMPARISON_CHANGED_MAIN)) {
				if (filteredSourceId < 0) {
					channelsRefreshNeeded = true;
				} else {
					if (filteredSourceId == scannedChannelAction.getSourceId()) {
						if ((filteredTranspId == scannedChannelAction
								.getTranspId()) || (filteredTranspId < 0)) {
							channelsRefreshNeeded = true;
						}
					}
				}
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
		IgnoredChannel lastPageTopChannel;

		if (channelsRefreshNeeded) {
			lastPageTopChannel = null;
			if (!channels.isEmpty()) {
				lastPageTopChannel = channels.get((scrollerPage - 1)
						* rowsPerPage);
			}

			retrieveAllChannels();

			if (!channels.isEmpty()) {
				if (lastPageTopChannel != null) {
					if (ignoredChannelRepository.findById(lastPageTopChannel
							.getId()) != null) {
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
		channels = ignoredChannelRepository.findAll(filteredSourceId,
				filteredTranspId, comparisonFilter);
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

	public List<IgnoredChannel> getChannels() {

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

	public List<IgnoredChannel> getCheckedChannels() {

		return checkedChannels;
	}

	public int getComparisonFilter() {

		return comparisonFilter;
	}

	public void setComparisonFilter(int comparisonFilter) {
		this.comparisonFilter = comparisonFilter;
	}

	public IgnoredChannel getEditedChannel() {

		return editedChannel;
	}

	public void setEditedChannel(IgnoredChannel editedChannel) {
		this.editedChannel = editedChannel;
	}

	public ScannedChannel getComparedScannedChannel() {

		return comparedScannedChannel;
	}

	public void setComparedScannedChannel(ScannedChannel comparedScannedChannel) {
		this.comparedScannedChannel = comparedScannedChannel;
	}

}

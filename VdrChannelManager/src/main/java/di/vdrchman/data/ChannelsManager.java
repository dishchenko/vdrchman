package di.vdrchman.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import di.vdrchman.model.Channel;

@SessionScoped
@Named
public class ChannelsManager implements Serializable {

	private static final long serialVersionUID = -1754936915825346390L;

	@Inject
	private ChannelRepository channelRepository;

	// ID of the Source to filter Channel list on.
	// No filtering if it's negative.
	private long filteredSourceId = -1;
	// ID of the Transponder to filter Channel list on.
	// No filtering if it's negative.
	private long filteredTranspId = -1;

	// (Filtered) Channel list for the current application user
	private List<Channel> channels;

	// Number of table rows per page
	private final int rowsPerPage = 15;
	// Current table scroller page 
	private int scrollerPage = 1;

	// Map of Channel IDs and checked checkboxes
	private Map<Long, Boolean> channelCheckboxes = new HashMap<Long, Boolean>();
	// List of checked Channels built on checkboxes map
	private List<Channel> checkedChannels = new ArrayList<Channel>();

	// Clear the list of checked Channels
	public void clearCheckedChannels() {
		checkedChannels.clear();
	}

	// Clear the map of Channel checkboxes
	public void clearChannelCheckboxes() {
		channelCheckboxes.clear();
	}

	// (Re)Fill in the Channel list
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

}

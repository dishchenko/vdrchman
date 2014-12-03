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
import di.vdrchman.model.Group;
import di.vdrchman.model.Transponder;

@SessionScoped
@Named
public class ChannelsManager implements Serializable {

	private static final long serialVersionUID = -1754936915825346390L;

	@Inject
	private TransponderRepository transponderRepository;

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

	// The Channel which the user is going to add/update
	private Channel editedChannel = new Channel();

	// Sequence number of the edited Channel
	private int editedChannelSeqno;

	// The "clipboard": the place to store the Channel copied by user
	private Channel copiedChannel = null;

	// Clear the list of checked Channels
	public void clearCheckedChannels() {
		checkedChannels.clear();
	}

	// Clear the map of Channel checkboxes
	public void clearChannelCheckboxes() {
		channelCheckboxes.clear();
	}

	// Calculate the sequence number for the Channel to be placed on top
	// of the current Channel list. If current Channel list is not empty
	// then it is the sequence number of its top Channel. Otherwise it is
	// calculated the way the Channel to be placed right after
	// the last existing Channel of previous Transponders
	public int calculateOnTopSeqno() {
		int result;
		Transponder transponder;
		Integer maxSeqnoOnTransponder;

		// TODO add filteredSourceId processing

		if (channels.isEmpty()) {
			result = 1;
			if (filteredTranspId >= 0) {
				transponder = transponderRepository
						.findBySeqno(transponderRepository
								.getSeqno(transponderRepository
										.findById(filteredTranspId)) - 1);
				while (transponder != null) {
					maxSeqnoOnTransponder = channelRepository
							.findMaxSeqno(transponder.getId());
					if (maxSeqnoOnTransponder != null) {
						result = maxSeqnoOnTransponder + 1;
						break;
					}
					transponder = transponderRepository
							.findBySeqno(transponderRepository
									.getSeqno(transponder) - 1);
				}
			}
		} else {
			result = channelRepository.getSeqno(channels.get(0));
		}

		return result;
	}

	// Build a string consisting of a comma delimited group descriptions
	public String buildGroupDescriptionsString(List<Group> groups) {
		StringBuilder sb;

		sb = new StringBuilder();

		if (!groups.isEmpty()) {
			for (Group group : groups) {
				sb.append(group.getDescription() + ", ");
			}

			sb.setLength(sb.length() - 2);
		}

		return sb.toString();
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

	public Channel getCopiedChannel() {

		return copiedChannel;
	}

	public void setCopiedChannel(Channel copiedChannel) {
		this.copiedChannel = copiedChannel;
	}

}

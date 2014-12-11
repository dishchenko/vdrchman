package di.vdrchman.controller;

import javax.enterprise.inject.Model;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.IgnoredChannelRepository;
import di.vdrchman.data.IgnoredChannelsManager;
import di.vdrchman.data.TransponderRepository;
import di.vdrchman.model.IgnoredChannel;

@Model
public class IgnoredChannelsBacking {

	@Inject
	private IgnoredChannelsManager ignoredChannelsManager;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private IgnoredChannelRepository ignoredChannelRepository;

	// The user is going to remove some checked channels
	public void intendRemoveChannels() {
		ignoredChannelsManager.collectCheckedChannels();
	}

	// Do that removal
	public void doRemoveChannels() {
		for (IgnoredChannel channel : ignoredChannelsManager
				.getCheckedChannels()) {
			ignoredChannelRepository.delete(channel);
		}
		ignoredChannelsManager.retrieveAllChannels();
		ignoredChannelsManager.clearCheckedChannels();
		ignoredChannelsManager.clearChannelCheckboxes();
	}

	// On changing the source filter selection clear the "clipboard"
	// if a source is selected and it is not the one taken channel
	// relates to. Clear the transponder filter selection if a source is
	// selected and it is not the one selected transponder relates to.
	// Also try to stay on the scroller page which includes the previous shown
	// page top channel
	public void onSourceMenuSelection(ValueChangeEvent event) {
		IgnoredChannel lastPageTopChannel;
		long filteredSourceId;

		lastPageTopChannel = null;
		if (!ignoredChannelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = ignoredChannelsManager.getChannels().get(
					(ignoredChannelsManager.getScrollerPage() - 1)
							* ignoredChannelsManager.getRowsPerPage());
		}
		filteredSourceId = (Long) event.getNewValue();
		ignoredChannelsManager.setFilteredSourceId(filteredSourceId);
		ignoredChannelsManager.setFilteredTranspId(-1);
		ignoredChannelsManager.retrieveAllChannels();
		ignoredChannelsManager.retrieveOrClearFilteredSourceTransponders();
		ignoredChannelsManager.clearCheckedChannels();
		ignoredChannelsManager.clearChannelCheckboxes();
		if (!ignoredChannelsManager.getChannels().isEmpty()) {
			if (lastPageTopChannel != null) {
				if (filteredSourceId == transponderRepository.findById(
						lastPageTopChannel.getTranspId()).getSourceId()) {
					ignoredChannelsManager.turnScrollerPage(lastPageTopChannel);
				} else {
					if (filteredSourceId < 0) {
						ignoredChannelsManager.turnScrollerPage(lastPageTopChannel);
					} else {
						ignoredChannelsManager.turnScrollerPage(ignoredChannelsManager
								.getChannels().get(0));
					}
				}
			} else {
				ignoredChannelsManager.turnScrollerPage(ignoredChannelsManager.getChannels()
						.get(0));
			}
		}
	}

	// On changing the transponder filter selection clear the "clipboard"
	// if a transponder is selected and it is not the one taken channel
	// relates to. Also try to stay on the scroller page which includes the
	// previous shown page top channel
	public void onTransponderMenuSelection(ValueChangeEvent event) {
		IgnoredChannel lastPageTopChannel;
		long filteredTranspId;

		lastPageTopChannel = null;
		if (!ignoredChannelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = ignoredChannelsManager.getChannels().get(
					(ignoredChannelsManager.getScrollerPage() - 1)
							* ignoredChannelsManager.getRowsPerPage());
		}
		filteredTranspId = (Long) event.getNewValue();
		ignoredChannelsManager.setFilteredTranspId(filteredTranspId);
		ignoredChannelsManager.retrieveAllChannels();
		ignoredChannelsManager.clearCheckedChannels();
		ignoredChannelsManager.clearChannelCheckboxes();
		if (!ignoredChannelsManager.getChannels().isEmpty()) {
			if (lastPageTopChannel != null) {
				if (filteredTranspId == lastPageTopChannel.getTranspId()) {
					ignoredChannelsManager.turnScrollerPage(lastPageTopChannel);
				} else {
					if (filteredTranspId < 0) {
						ignoredChannelsManager.turnScrollerPage(lastPageTopChannel);
					} else {
						ignoredChannelsManager.turnScrollerPage(ignoredChannelsManager
								.getChannels().get(0));
					}
				}
			} else {
				ignoredChannelsManager.turnScrollerPage(ignoredChannelsManager.getChannels()
						.get(0));
			}
		}
	}

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		ignoredChannelsManager.clearCheckedChannels();
		ignoredChannelsManager.clearChannelCheckboxes();
	}

}

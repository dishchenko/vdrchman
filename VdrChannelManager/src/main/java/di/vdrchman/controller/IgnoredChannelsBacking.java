package di.vdrchman.controller;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Model;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.IgnoredChannelRepository;
import di.vdrchman.data.IgnoredChannelsManager;
import di.vdrchman.data.ScannedChannelRepository;
import di.vdrchman.data.SourceRepository;
import di.vdrchman.data.TransponderRepository;
import di.vdrchman.event.IgnoredChannelAction;
import di.vdrchman.model.IgnoredChannel;
import di.vdrchman.model.ScannedChannel;
import di.vdrchman.model.Transponder;

@Model
public class IgnoredChannelsBacking {

	@Inject
	private IgnoredChannelsManager ignoredChannelsManager;

	@Inject
	private SourceRepository sourceRepository;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private IgnoredChannelRepository ignoredChannelRepository;

	@Inject
	private ScannedChannelRepository scannedChannelRepository;

	@Inject
	private Event<IgnoredChannelAction> ignoredChannelActionEvent;

	// The user is going to remove some checked channels
	public void intendRemoveChannels() {
		ignoredChannelsManager.collectCheckedChannels();
	}

	// Do that removal
	public void doRemoveChannels() {
		for (IgnoredChannel channel : ignoredChannelsManager
				.getCheckedChannels()) {
			ignoredChannelRepository.delete(channel);
			ignoredChannelActionEvent.fire(new IgnoredChannelAction(channel,
					IgnoredChannelAction.Action.DELETE));
		}
		ignoredChannelsManager.retrieveAllChannels();
		ignoredChannelsManager.clearCheckedChannels();
		ignoredChannelsManager.clearChannelCheckboxes();
	}

	// The user is going to update a channel
	public void intendUpdateChannel() {
		IgnoredChannel channel;
		Transponder transponder;

		ignoredChannelsManager.collectCheckedChannels();
		channel = ignoredChannelsManager.getCheckedChannels().get(0);
		ignoredChannelsManager.setEditedChannel(new IgnoredChannel(channel));
		transponder = transponderRepository.findById(channel.getTranspId());
		ignoredChannelsManager
				.setComparedScannedChannel(scannedChannelRepository
						.findBySourceFrequencyPolarizationStreamSidApid(
								sourceRepository.findById(
										transponder.getSourceId()).getName(),
								transponder.getFrequency(),
								transponder.getPolarization(),
								transponder.getStreamId(), channel.getSid(),
								channel.getApid()));
	}

	// Really updating the channel
	public void doUpdateChannel() {
		IgnoredChannel channel;
		ScannedChannel scannedChannel;

		channel = ignoredChannelsManager.getEditedChannel();
		scannedChannel = ignoredChannelsManager.getComparedScannedChannel();
		channel.setVpid(scannedChannel.getVpid());
		channel.setCaid(scannedChannel.getCaid());
		channel.setScannedName(scannedChannel.getScannedName());
		ignoredChannelRepository.update(channel);
		ignoredChannelActionEvent.fire(new IgnoredChannelAction(
				ignoredChannelsManager.getEditedChannel(),
				IgnoredChannelAction.Action.UPDATE));
		ignoredChannelsManager.retrieveAllChannels();
		ignoredChannelsManager.clearCheckedChannels();
		ignoredChannelsManager.clearChannelCheckboxes();
	}

	// On changing the source filter selection clear the transponder filter
	// selection if a source is selected and it is not the one selected
	// transponder relates to.
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
						ignoredChannelsManager
								.turnScrollerPage(lastPageTopChannel);
					} else {
						ignoredChannelsManager
								.turnScrollerPage(ignoredChannelsManager
										.getChannels().get(0));
					}
				}
			} else {
				ignoredChannelsManager.turnScrollerPage(ignoredChannelsManager
						.getChannels().get(0));
			}
		}
	}

	// On changing the transponder filter selection try to stay on the scroller
	// page which includes the previous shown page top channel
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
						ignoredChannelsManager
								.turnScrollerPage(lastPageTopChannel);
					} else {
						ignoredChannelsManager
								.turnScrollerPage(ignoredChannelsManager
										.getChannels().get(0));
					}
				}
			} else {
				ignoredChannelsManager.turnScrollerPage(ignoredChannelsManager
						.getChannels().get(0));
			}
		}
	}

	// On changing the comparison result filter selection try to stay
	// on the scroller page which includes the previous shown page top channel
	public void onComparisonMenuSelection(ValueChangeEvent event) {
		IgnoredChannel lastPageTopChannel;
		int comparisonFilter;

		lastPageTopChannel = null;
		if (!ignoredChannelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = ignoredChannelsManager.getChannels().get(
					(ignoredChannelsManager.getScrollerPage() - 1)
							* ignoredChannelsManager.getRowsPerPage());
		}
		comparisonFilter = (Integer) event.getNewValue();
		ignoredChannelsManager.setComparisonFilter(comparisonFilter);
		ignoredChannelsManager.retrieveAllChannels();
		ignoredChannelsManager.clearCheckedChannels();
		ignoredChannelsManager.clearChannelCheckboxes();
		if (!ignoredChannelsManager.getChannels().isEmpty()) {
			if (lastPageTopChannel != null) {
				if (!ignoredChannelsManager
						.turnScrollerPage(lastPageTopChannel)) {
					ignoredChannelsManager
							.turnScrollerPage(ignoredChannelsManager
									.getChannels().get(0));
				}
			} else {
				ignoredChannelsManager.turnScrollerPage(ignoredChannelsManager
						.getChannels().get(0));
			}
		}
	}

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		ignoredChannelsManager.clearCheckedChannels();
		ignoredChannelsManager.clearChannelCheckboxes();
	}

}

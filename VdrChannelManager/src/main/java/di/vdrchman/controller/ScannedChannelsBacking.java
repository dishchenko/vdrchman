package di.vdrchman.controller;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Model;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.FilesManager;
import di.vdrchman.data.Scan;
import di.vdrchman.data.ScannedChannelsManager;
import di.vdrchman.data.SourceRepository;
import di.vdrchman.data.TransponderRepository;
import di.vdrchman.event.ScannedChannelAction;
import di.vdrchman.model.ScannedChannel;
import di.vdrchman.model.Source;
import di.vdrchman.model.Transponder;

@Model
public class ScannedChannelsBacking {

	@Inject
	private ScannedChannelsManager scannedChannelsManager;

	@Inject
	private FilesManager filesManager;

	@Inject
	private SourceRepository sourceRepository;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private Event<ScannedChannelAction> scannedChannelActionEvent;

	// Process all uploaded scanned channel files (scans) one by one. Store
	// results of processing in the scanned channels table
	public void processUploadedScans() {
		scannedChannelsManager.clearScanProcessingReports();
		for (Scan scan : filesManager.getScans()) {
			scannedChannelsManager.addScanProcessingReport(
					scan.getFileName(),
					scannedChannelsManager.processScanData(
							scan.buildSourceName(), scan.getData()));
		}
		scannedChannelsManager.retrieveAllChannels();
		filesManager.clearScans();
		scannedChannelActionEvent.fire(new ScannedChannelAction(
				ScannedChannelAction.Action.SCAN_PROCESSED));
	}

	// The user is going to add to main channel list the new channel based on
	// the scanned channel data
	public void intendAddToChannels() {
		scannedChannelsManager.collectCheckedChannels();
	}

	// The user is going to add to ignored channel list the new channel based on
	// the scanned channel data
	public void intendAddToIgnoredChannels() {
		scannedChannelsManager.collectCheckedChannels();
	}

	// The user is going to update channel in main channel list based on
	// the scanned channel data
	public void intendUpdateChannels() {
		scannedChannelsManager.collectCheckedChannels();
	}

	// The user is going to update channel in ignored channel list based on
	// the scanned channel data
	public void intendUpdateIgnoredChannels() {
		scannedChannelsManager.collectCheckedChannels();
	}

	// On changing the source filter selection clear the transponder filter
	// selection if a source is selected and it is not the one selected
	// transponder relates to.
	// Also try to stay on the scroller page which includes the previous shown
	// page top channel
	public void onSourceMenuSelection(ValueChangeEvent event) {
		ScannedChannel lastPageTopChannel;
		long filteredSourceId;
		Source source;

		lastPageTopChannel = null;
		if (!scannedChannelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = scannedChannelsManager.getChannels().get(
					(scannedChannelsManager.getScrollerPage() - 1)
							* scannedChannelsManager.getRowsPerPage());
		}
		filteredSourceId = (Long) event.getNewValue();
		scannedChannelsManager.setFilteredSourceId(filteredSourceId);
		scannedChannelsManager.setFilteredTranspId(-1);
		scannedChannelsManager.retrieveAllChannels();
		scannedChannelsManager.retrieveOrClearFilteredSourceTransponders();
		scannedChannelsManager.clearCheckedChannels();
		scannedChannelsManager.clearChannelCheckboxes();
		if (!scannedChannelsManager.getChannels().isEmpty()) {
			if (lastPageTopChannel != null) {
				source = sourceRepository.findByName(lastPageTopChannel
						.getSourceName());
				if (source != null) {
					if (filteredSourceId == source.getId()) {
						scannedChannelsManager
								.turnScrollerPage(lastPageTopChannel);
					} else {
						if (filteredSourceId < 0) {
							scannedChannelsManager
									.turnScrollerPage(lastPageTopChannel);
						} else {
							scannedChannelsManager
									.turnScrollerPage(scannedChannelsManager
											.getChannels().get(0));
						}
					}
				}
			} else {
				scannedChannelsManager.turnScrollerPage(scannedChannelsManager
						.getChannels().get(0));
			}
		}
	}

	// On changing the transponder filter selection try to stay on the scroller
	// page which includes the previous shown page top channel
	public void onTransponderMenuSelection(ValueChangeEvent event) {
		ScannedChannel lastPageTopChannel;
		long filteredTranspId;
		Source source;
		Transponder transponder;

		lastPageTopChannel = null;
		if (!scannedChannelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = scannedChannelsManager.getChannels().get(
					(scannedChannelsManager.getScrollerPage() - 1)
							* scannedChannelsManager.getRowsPerPage());
		}
		filteredTranspId = (Long) event.getNewValue();
		scannedChannelsManager.setFilteredTranspId(filteredTranspId);
		scannedChannelsManager.retrieveAllChannels();
		scannedChannelsManager.clearCheckedChannels();
		scannedChannelsManager.clearChannelCheckboxes();
		if (!scannedChannelsManager.getChannels().isEmpty()) {
			if (lastPageTopChannel != null) {
				source = sourceRepository.findByName(lastPageTopChannel
						.getSourceName());
				if (source != null) {
					transponder = transponderRepository
							.findBySourceFrequencyPolarizationStream(
									source.getId(),
									lastPageTopChannel.getFrequency(),
									lastPageTopChannel.getPolarization(),
									lastPageTopChannel.getStreamId());
					if (transponder != null) {
						if (filteredTranspId == transponder.getId()) {
							scannedChannelsManager
									.turnScrollerPage(lastPageTopChannel);
						} else {
							if (filteredTranspId < 0) {
								scannedChannelsManager
										.turnScrollerPage(lastPageTopChannel);
							} else {
								scannedChannelsManager
										.turnScrollerPage(scannedChannelsManager
												.getChannels().get(0));
							}
						}
					}
				}
			} else {
				scannedChannelsManager.turnScrollerPage(scannedChannelsManager
						.getChannels().get(0));
			}
		}
	}

	// On changing the comparison result filter selection try to stay
	// on the scroller page which includes the previous shown page top channel
	public void onComparisonMenuSelection(ValueChangeEvent event) {
		ScannedChannel lastPageTopChannel;
		int comparisonFilter;

		lastPageTopChannel = null;
		if (!scannedChannelsManager.getChannels().isEmpty()) {
			lastPageTopChannel = scannedChannelsManager.getChannels().get(
					(scannedChannelsManager.getScrollerPage() - 1)
							* scannedChannelsManager.getRowsPerPage());
		}
		comparisonFilter = (Integer) event.getNewValue();
		scannedChannelsManager.setComparisonFilter(comparisonFilter);
		scannedChannelsManager.retrieveAllChannels();
		scannedChannelsManager.clearCheckedChannels();
		scannedChannelsManager.clearChannelCheckboxes();
		if (!scannedChannelsManager.getChannels().isEmpty()) {
			if (lastPageTopChannel != null) {
				if (!scannedChannelsManager
						.turnScrollerPage(lastPageTopChannel)) {
					scannedChannelsManager
							.turnScrollerPage(scannedChannelsManager
									.getChannels().get(0));
				}
			} else {
				scannedChannelsManager.turnScrollerPage(scannedChannelsManager
						.getChannels().get(0));
			}
		}
	}

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		scannedChannelsManager.clearCheckedChannels();
		scannedChannelsManager.clearChannelCheckboxes();
	}

}

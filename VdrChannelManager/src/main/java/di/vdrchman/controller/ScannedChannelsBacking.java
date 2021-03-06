package di.vdrchman.controller;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Model;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.ChannelRepository;
import di.vdrchman.data.FilesManager;
import di.vdrchman.data.GroupRepository;
import di.vdrchman.data.IgnoredChannelRepository;
import di.vdrchman.data.Scan;
import di.vdrchman.data.ScannedChannelsManager;
import di.vdrchman.data.SourceRepository;
import di.vdrchman.data.TransponderRepository;
import di.vdrchman.event.ScannedChannelAction;
import di.vdrchman.model.Channel;
import di.vdrchman.model.Group;
import di.vdrchman.model.IgnoredChannel;
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
	private ChannelRepository channelRepository;

	@Inject
	private IgnoredChannelRepository ignoredChannelRepository;

	@Inject
	private GroupRepository groupRepository;

	@Inject
	private Event<ScannedChannelAction> scannedChannelActionEvent;

	// Process all uploaded scanned channel files (scans) one by one. Store
	// results of processing in the scanned channels table
	public void processUploadedScans() {
		String scanSourceName;
		Source scanSource;

		scannedChannelsManager.clearScanProcessingReports();
		for (Scan scan : filesManager.getScans()) {
			scanSourceName = scan.buildSourceName();
			scannedChannelsManager.addScanProcessingReport(
					scan.getFileName(),
					scannedChannelsManager.processScanData(scanSourceName,
							scan.getData()));
			scanSource = sourceRepository.findByName(scanSourceName);
			if (scanSource != null) {
				scannedChannelActionEvent.fire(new ScannedChannelAction(
						ScannedChannelAction.Action.SCAN_PROCESSED, scanSource
								.getId(), -1));
			}
		}
		scannedChannelsManager.retrieveAllChannels();
		filesManager.clearScans();
	}

	// The user is going to add to main channel list the new channel based on
	// the scanned channel data
	public void intendAddToChannels() {
		ScannedChannel workingChannel;
		Source workingChannelSource;
		Transponder workingChannelTransponder;
		Channel editedChannel;
		List<Group> groups;

		scannedChannelsManager.collectCheckedChannels();
		workingChannel = scannedChannelsManager.getCheckedChannels().get(0);
		scannedChannelsManager.setWorkingChannel(workingChannel);
		workingChannelSource = sourceRepository.findByName(workingChannel
				.getSourceName());
		scannedChannelsManager.setWorkingChannelSource(workingChannelSource);
		if (workingChannelSource != null) {
			workingChannelTransponder = transponderRepository
					.findBySourceFrequencyPolarizationStream(
							workingChannelSource.getId(),
							workingChannel.getFrequency(),
							workingChannel.getPolarization(),
							workingChannel.getStreamId());
		} else {
			workingChannelTransponder = null;
		}
		scannedChannelsManager
				.setWorkingChannelTransponder(workingChannelTransponder);
		editedChannel = new Channel();
		editedChannel.setVpid(workingChannel.getVpid());
		editedChannel.setVenc(workingChannel.getVenc());
		editedChannel.setAenc(workingChannel.getAenc());
		scannedChannelsManager.setEditedChannel(editedChannel);
		groups = groupRepository.findAll();
		if (groups.size() == 1) {
			scannedChannelsManager
					.setAddedChannelGroupId(groups.get(0).getId());
		} else {
			scannedChannelsManager.setAddedChannelGroupId(-1);
		}
	}

	// Really adding the new channel based on the scanned channel data to main
	// channel list
	public void doAddToChannels() {
		Channel editedChannel;
		Transponder workingChannelTransponder;
		ScannedChannel workingChannel;
		long channelGroupId;
		List<Group> channelGroups;

		editedChannel = scannedChannelsManager.getEditedChannel();
		workingChannelTransponder = scannedChannelsManager
				.getWorkingChannelTransponder();
		editedChannel.setTranspId(workingChannelTransponder.getId());
		workingChannel = scannedChannelsManager.getWorkingChannel();
		editedChannel.setSid(workingChannel.getSid());
		editedChannel.setPcr(workingChannel.getPcr());
		editedChannel.setApid(workingChannel.getApid());
		editedChannel.setTpid(workingChannel.getTpid());
		editedChannel.setCaid(workingChannel.getCaid());
		editedChannel.setRid(workingChannel.getRid());
		editedChannel.setScannedName(workingChannel.getScannedName());
		editedChannel.setProviderName(workingChannel.getProviderName());
		channelRepository.add(editedChannel,
				channelRepository.findMaxSeqno(-1) + 1);
		scannedChannelActionEvent.fire(new ScannedChannelAction(
				ScannedChannelAction.Action.CHANNEL_ADDED,
				workingChannelTransponder.getSourceId(),
				workingChannelTransponder.getId()));
		channelGroupId = scannedChannelsManager.getAddedChannelGroupId();
		if (channelGroupId != -1) {
			channelGroups = new ArrayList<Group>();
			channelGroups.add(groupRepository.findById(channelGroupId));
			channelRepository
					.updateGroups(editedChannel.getId(), channelGroups);
			scannedChannelActionEvent.fire(new ScannedChannelAction(
					ScannedChannelAction.Action.GROUPS_UPDATED,
					workingChannelTransponder.getSourceId(),
					workingChannelTransponder.getId()));
		}
		scannedChannelsManager.retrieveAllChannels();
		scannedChannelsManager.clearCheckedChannels();
		scannedChannelsManager.clearChannelCheckboxes();
		scannedChannelsManager.adjustLastScrollerPage();
	}

	// The user is going to add to ignored channel list new channels based on
	// the scanned channel data
	public void intendAddToIgnoredChannels() {
		scannedChannelsManager.collectCheckedChannels();
	}

	// Really adding new channels based on the scanned channel data to
	// ignored channel list
	public void doAddToIgnoredChannels() {
		Source source;
		Transponder transponder;
		IgnoredChannel ignoredChannel;

		for (ScannedChannel scannedChannel : scannedChannelsManager
				.getCheckedChannels()) {
			source = sourceRepository
					.findByName(scannedChannel.getSourceName());
			if (source != null) {
				transponder = transponderRepository
						.findBySourceFrequencyPolarizationStream(
								source.getId(), scannedChannel.getFrequency(),
								scannedChannel.getPolarization(),
								scannedChannel.getStreamId());
				if (transponder != null) {
					ignoredChannel = new IgnoredChannel();
					ignoredChannel.setTranspId(transponder.getId());
					ignoredChannel.setSid(scannedChannel.getSid());
					ignoredChannel.setVpid(scannedChannel.getVpid());
					ignoredChannel.setApid(scannedChannel.getApid());
					ignoredChannel.setCaid(scannedChannel.getCaid());
					ignoredChannel.setScannedName(scannedChannel
							.getScannedName());
					ignoredChannel.setProviderName(scannedChannel
							.getProviderName());
					ignoredChannelRepository.add(ignoredChannel);
					scannedChannelActionEvent.fire(new ScannedChannelAction(
							ScannedChannelAction.Action.IGNORED_CHANNEL_ADDED,
							transponder.getSourceId(), transponder.getId()));
				}
			}
		}
		scannedChannelsManager.retrieveAllChannels();
		scannedChannelsManager.clearCheckedChannels();
		scannedChannelsManager.clearChannelCheckboxes();
		scannedChannelsManager.adjustLastScrollerPage();
	}

	// The user is going to update channel in main channel list based on
	// the scanned channel data
	public void intendUpdateInChannels() {
		ScannedChannel workingChannel;
		Source workingChannelSource;
		Transponder workingChannelTransponder;

		scannedChannelsManager.collectCheckedChannels();
		workingChannel = scannedChannelsManager.getCheckedChannels().get(0);
		scannedChannelsManager.setWorkingChannel(workingChannel);
		workingChannelSource = sourceRepository.findByName(workingChannel
				.getSourceName());
		scannedChannelsManager.setWorkingChannelSource(workingChannelSource);
		workingChannelTransponder = transponderRepository
				.findBySourceFrequencyPolarizationStream(
						workingChannelSource.getId(),
						workingChannel.getFrequency(),
						workingChannel.getPolarization(),
						workingChannel.getStreamId());
		scannedChannelsManager
				.setWorkingChannelTransponder(workingChannelTransponder);
		scannedChannelsManager.setEditedChannel(new Channel(channelRepository
				.findByTransponderSidApid(workingChannelTransponder.getId(),
						workingChannel.getSid(), workingChannel.getApid())));
	}

	// Really updating the channel in main list based on the scanned channel
	// data
	public void doUpdateInChannels() {
		Channel editedChannel;
		ScannedChannel workingChannel;
		Transponder editedChannelTransponder;

		editedChannel = scannedChannelsManager.getEditedChannel();
		workingChannel = scannedChannelsManager.getWorkingChannel();
		editedChannel.setPcr(workingChannel.getPcr());
		editedChannel.setRid(workingChannel.getRid());
		editedChannel.setScannedName(workingChannel.getScannedName());
		editedChannel.setProviderName(workingChannel.getProviderName());
		channelRepository.update(editedChannel);
		editedChannelTransponder = transponderRepository.findById(editedChannel
				.getTranspId());
		scannedChannelActionEvent.fire(new ScannedChannelAction(
				ScannedChannelAction.Action.CHANNEL_UPDATED,
				editedChannelTransponder.getSourceId(),
				editedChannelTransponder.getId()));
		scannedChannelsManager.retrieveAllChannels();
		scannedChannelsManager.clearCheckedChannels();
		scannedChannelsManager.clearChannelCheckboxes();
		scannedChannelsManager.adjustLastScrollerPage();
	}

	// Remove the channel from main list
	public void doRemoveFromChannels() {
		Channel editedChannel;
		Transponder editedChannelTransponder;

		editedChannel = scannedChannelsManager.getEditedChannel();
		channelRepository.delete(editedChannel);
		editedChannelTransponder = transponderRepository.findById(editedChannel
				.getTranspId());
		scannedChannelActionEvent.fire(new ScannedChannelAction(
				ScannedChannelAction.Action.CHANNEL_REMOVED,
				editedChannelTransponder.getSourceId(),
				editedChannelTransponder.getId()));
		scannedChannelsManager.retrieveAllChannels();
		scannedChannelsManager.clearCheckedChannels();
		scannedChannelsManager.clearChannelCheckboxes();
		scannedChannelsManager.adjustLastScrollerPage();
	}

	// The user is going to update channel in ignored channel list based on
	// the scanned channel data
	public void intendUpdateInIgnoredChannels() {
		ScannedChannel workingChannel;
		Source workingChannelSource;
		Transponder workingChannelTransponder;

		scannedChannelsManager.collectCheckedChannels();
		workingChannel = scannedChannelsManager.getCheckedChannels().get(0);
		scannedChannelsManager.setWorkingChannel(workingChannel);
		workingChannelSource = sourceRepository.findByName(workingChannel
				.getSourceName());
		scannedChannelsManager.setWorkingChannelSource(workingChannelSource);
		workingChannelTransponder = transponderRepository
				.findBySourceFrequencyPolarizationStream(
						workingChannelSource.getId(),
						workingChannel.getFrequency(),
						workingChannel.getPolarization(),
						workingChannel.getStreamId());
		scannedChannelsManager
				.setWorkingChannelTransponder(workingChannelTransponder);
		scannedChannelsManager.setEditedIgnoredChannel(new IgnoredChannel(
				ignoredChannelRepository.findByTransponderSidApid(
						workingChannelTransponder.getId(),
						workingChannel.getSid(), workingChannel.getApid())));
	}

	// Really updating the channel in ignored channel list based on the scanned
	// channel data
	public void doUpdateInIgnoredChannels() {
		IgnoredChannel editedIgnoredChannel;
		ScannedChannel workingChannel;
		Transponder editedIgnoredChannelTransponder;

		editedIgnoredChannel = scannedChannelsManager.getEditedIgnoredChannel();
		workingChannel = scannedChannelsManager.getWorkingChannel();
		editedIgnoredChannel.setVpid(workingChannel.getVpid());
		editedIgnoredChannel.setCaid(workingChannel.getCaid());
		editedIgnoredChannel.setScannedName(workingChannel.getScannedName());
		ignoredChannelRepository.update(editedIgnoredChannel);
		editedIgnoredChannelTransponder = transponderRepository
				.findById(editedIgnoredChannel.getTranspId());
		scannedChannelActionEvent.fire(new ScannedChannelAction(
				ScannedChannelAction.Action.IGNORED_CHANNEL_UPDATED,
				editedIgnoredChannelTransponder.getSourceId(),
				editedIgnoredChannelTransponder.getId()));
		scannedChannelsManager.retrieveAllChannels();
		scannedChannelsManager.clearCheckedChannels();
		scannedChannelsManager.clearChannelCheckboxes();
		scannedChannelsManager.adjustLastScrollerPage();
	}

	// Remove the channel from ignored channel list
	public void doRemoveFromIgnoredChannels() {
		IgnoredChannel editedIgnoredChannel;
		Transponder editedIgnoredChannelTransponder;

		editedIgnoredChannel = scannedChannelsManager.getEditedIgnoredChannel();
		ignoredChannelRepository.delete(editedIgnoredChannel);
		editedIgnoredChannelTransponder = transponderRepository
				.findById(editedIgnoredChannel.getTranspId());
		scannedChannelActionEvent.fire(new ScannedChannelAction(
				ScannedChannelAction.Action.IGNORED_CHANNEL_REMOVED,
				editedIgnoredChannelTransponder.getSourceId(),
				editedIgnoredChannelTransponder.getId()));
		scannedChannelsManager.retrieveAllChannels();
		scannedChannelsManager.clearCheckedChannels();
		scannedChannelsManager.clearChannelCheckboxes();
		scannedChannelsManager.adjustLastScrollerPage();
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

package di.vdrchman.data;

import static di.vdrchman.util.Tools.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import javax.inject.Named;

import di.vdrchman.event.ChannelAction;
import di.vdrchman.event.IgnoredChannelAction;
import di.vdrchman.event.SourceAction;
import di.vdrchman.event.TransponderAction;
import di.vdrchman.model.Channel;
import di.vdrchman.model.IgnoredChannel;
import di.vdrchman.model.ScannedChannel;
import di.vdrchman.model.Source;
import di.vdrchman.model.Transponder;
import di.vdrchman.util.Tools;

@SessionScoped
@Named
public class ScannedChannelsManager implements Serializable {

	private static final long serialVersionUID = 6811121080345239295L;

	@Inject
	private SourceRepository sourceRepository;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private ScannedChannelRepository scannedChannelRepository;

	@Inject
	private ChannelRepository channelRepository;

	@Inject
	private IgnoredChannelRepository ignoredChannelRepository;

	@Inject
	Logger logger;

	// ID of the source to filter scanned channel list on.
	// No filtering if it's negative.
	private long filteredSourceId = -1;
	// ID of the transponder to filter scanned channel list on.
	// No filtering if it's negative.
	private long filteredTranspId = -1;

	// List of filtered source transponders
	private List<Transponder> filteredSourceTransponders = new ArrayList<Transponder>();

	// Indicates that filtered source transponder list refresh is suggested
	private boolean filteredSourceTranspondersRefreshNeeded = false;

	// (Filtered) Scanned channel list for the current application user
	private List<ScannedChannel> channels;

	// Indicates that channels list refresh is suggested
	private boolean channelsRefreshNeeded = false;

	// Number of table rows per page
	private final int rowsPerPage = 10;
	// Current table scroller page
	private int scrollerPage = 1;

	// Map of channel IDs and checked checkboxes
	private Map<Long, Boolean> channelCheckboxes = new HashMap<Long, Boolean>();
	// List of checked channels built on checkboxes map
	private List<ScannedChannel> checkedChannels = new ArrayList<ScannedChannel>();

	// List of arrays containing information on how scans were processed.
	// First element of each array contains the name of processed scan file,
	// second - processing details
	private List<String[]> scanProcessingReports = new ArrayList<String[]>();

	// Comparison result filter value. See Tools.COMPARISON_*
	private int comparisonFilter = Tools.COMPARISON_NONE;

	// Scanned channel selected for adding to other channel lists or
	// updating their content
	private ScannedChannel workingChannel;

	// Source corresponding to the selected scanned channel
	private Source workingChannelSource;

	// Transponder corresponding to the selected scanned channel
	private Transponder workingChannelTransponder;

	// Channel which is to be added or updated in the main channel list
	private Channel editedChannel;

	// Channel which is to be added or updated in the ignored channel list
	private IgnoredChannel editedIgnoredChannel;

	// ID of the group the newly added to main list channel will be a member of.
	// -1 if none
	private long addedChannelGroupId = -1;

	// Fill in checkedChannels list with channels corresponding
	// to checkboxes checked in the data table on the page
	public void collectCheckedChannels() {
		clearCheckedChannels();

		for (ScannedChannel channel : channels) {
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
	public boolean turnScrollerPage(ScannedChannel channel) {
		boolean result;
		List<ScannedChannel> channels;
		int i;

		result = false;

		channels = scannedChannelRepository.findAll(filteredSourceId,
				filteredTranspId, comparisonFilter);
		i = 0;
		for (ScannedChannel theChannel : channels) {
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

	// Cleanup the ScannedChannelsManager's data on SourceAction if needed
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

	// Cleanup the ScannedChannelsManager's data on TransponderAction if needed
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

	// Cleanup the ScannedChannelsManager's data on ChannelAction if needed
	public void onChannelAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final ChannelAction channelAction) {
		long transpId;
		long sourceId;

		if (comparisonFilter == COMPARISON_NEW) {
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
		}

		if (comparisonFilter == COMPARISON_CHANGED_MAIN) {
			if ((channelAction.getAction() == ChannelAction.Action.UPDATE)
					|| (channelAction.getAction() == ChannelAction.Action.DELETE)) {
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
			}
		}
	}

	// Cleanup the ScannedChannelsManager's data on IgnoredChannelAction if
	// needed
	public void onIgnoredChannelAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final IgnoredChannelAction ignoredChannelAction) {
		long transpId;
		long sourceId;

		if (comparisonFilter == COMPARISON_NEW) {
			transpId = ignoredChannelAction.getChannel().getTranspId();
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
		}

		if (comparisonFilter == COMPARISON_CHANGED_IGNORED) {
			if ((ignoredChannelAction.getAction() == IgnoredChannelAction.Action.UPDATE)
					|| (ignoredChannelAction.getAction() == IgnoredChannelAction.Action.DELETE)) {
				transpId = ignoredChannelAction.getChannel().getTranspId();
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
		ScannedChannel lastPageTopChannel;

		if (channelsRefreshNeeded) {
			lastPageTopChannel = null;
			if (!channels.isEmpty()) {
				lastPageTopChannel = channels.get((scrollerPage - 1)
						* rowsPerPage);
			}

			retrieveAllChannels();

			if (!channels.isEmpty()) {
				if (lastPageTopChannel != null) {
					if (scannedChannelRepository.findById(lastPageTopChannel
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

	// Process scan data and store results in the scanned channels table.
	// Update channels in main channel list and ignored channels if updates are
	// "technical" i.e. programs not changed
	public String processScanData(String scanSourceName, byte[] data) {
		String result;
		Source source;
		long sourceId;
		int lineNo;
		Integer lastFrequency;
		String lastPolarization;
		Integer lastStreamId;
		BufferedReader br;
		String line;
		String[] splitLine;
		String[] snInfo;
		String scannedName;
		String providerName;
		Integer frequency;
		String polarization;
		Integer streamId;
		int streamIdPos;
		Integer dvbsGen;
		String sourceName;
		Integer symbolRate;
		String[] vInfo;
		Integer venc;
		Integer vpid;
		Integer pcr;
		String[] tInfo;
		Integer tpid;
		String caid;
		String[] caidInfo;
		SortedSet<String> caidSet;
		StringBuilder caidSb;
		Integer sid;
		Integer nid;
		Integer tid;
		Integer rid;
		String[] aStreams;
		String[] aInfo;
		Integer apid;
		Integer aenc;
		ScannedChannel scannedChannel;
		Transponder transponder;
		boolean transponderChanged;
		List<Channel> forcedUpdateChannels;
		List<IgnoredChannel> forcedUpdateIgnoredChannels;

		result = "OK";

		source = sourceRepository.findByName(scanSourceName);
		if (source != null) {
			sourceId = source.getId();

			lineNo = 0;

			try {
				scannedChannelRepository.makeNotRefreshed(scanSourceName);

				lastFrequency = 0;
				lastPolarization = "";
				lastStreamId = 0;

				br = new BufferedReader(new InputStreamReader(
						new ByteArrayInputStream(data), "ISO-8859-5"));

				while ((line = br.readLine()) != null) {

					++lineNo;

					if (line.length() == 0) {
						continue;
					}
					if (line.charAt(0) == '#') {
						continue;
					}
					splitLine = line.split(":");

					if (splitLine.length != 13) {
						result = "Error: line " + lineNo + ": invalid format";

						break;
					}

					snInfo = splitLine[0].split(";");
					scannedName = snInfo[0];
					if (scannedName.length() > 50) {
						scannedName = scannedName.substring(0, 50);
					}
					if (snInfo.length == 2) {
						providerName = snInfo[1];
						if (providerName.length() > 50) {
							providerName = providerName.substring(0, 50);
						}
					} else {
						providerName = null;
					}

					frequency = Integer.parseInt(splitLine[1]);

					polarization = null;
					if (splitLine[2].contains("H")) {
						polarization = "H";
					}
					if (splitLine[2].contains("V")) {
						polarization = "V";
					}
					if (splitLine[2].contains("L")) {
						polarization = "L";
					}
					if (splitLine[2].contains("R")) {
						polarization = "R";
					}

					streamId = null;
					streamIdPos = splitLine[2].indexOf("X");
					if (streamIdPos >= 0) {
						streamId = NumberFormat.getInstance()
								.parse(splitLine[2].substring(streamIdPos + 1))
								.intValue();
					}

					dvbsGen = null;
					if (splitLine[2].contains("S0")) {
						dvbsGen = 1;
					}
					if (splitLine[2].contains("S1")) {
						dvbsGen = 2;
					}

					sourceName = splitLine[3];

					symbolRate = Integer.parseInt(splitLine[4]);

					vInfo = splitLine[5].split("=");
					if (vInfo.length == 2) {
						venc = Integer.parseInt(vInfo[1]);
					} else {
						venc = 0;
					}
					if (venc == 0) {
						venc = null;
					}
					vInfo = vInfo[0].split("\\+");
					vpid = Integer.parseInt(vInfo[0]);
					if (vpid == 0) {
						vpid = null;
					}
					if (vInfo.length == 2) {
						pcr = Integer.parseInt(vInfo[1]);
					} else {
						pcr = 0;
					}
					if (pcr == 0) {
						pcr = null;
					}

					tInfo = splitLine[7].split(";");
					tpid = Integer.parseInt(tInfo[0]);
					if (tpid == 0) {
						tpid = null;
					}

					caid = splitLine[8];
					try {
						caidInfo = caid.split(",");
						caidSet = new TreeSet<String>();
						for (String caidItem : caidInfo) {
							caidSet.add(caidItem);
						}
						caidSb = new StringBuilder();
						for (String caidItem : caidSet) {
							caidSb.append(caidItem).append(",");
						}
						caidSb.setLength(caidSb.length() - 1);
						caid = caidSb.toString();
					} catch (NumberFormatException ex) {
						// do nothing
					}
					if (caid.length() > 50) {
						caid = caid.substring(0, 50);
					}
					try {
						if (Integer.parseInt(caid) == 0) {
							caid = null;
						}
					} catch (NumberFormatException ex) {
						// do nothing
					}

					sid = Integer.parseInt(splitLine[9]);

					nid = Integer.parseInt(splitLine[10]);
					if (nid == 0) {
						nid = null;
					}

					tid = Integer.parseInt(splitLine[11]);
					if (tid == 0) {
						tid = null;
					}

					rid = Integer.parseInt(splitLine[12]);
					if (rid == 0) {
						rid = null;
					}

					aStreams = splitLine[6].split(",|;");
					for (String aStream : aStreams) {
						aInfo = aStream.split("=");
						apid = Integer.parseInt(aInfo[0]);
						aenc = 0;
						if (aInfo.length == 2) {
							aInfo = aInfo[1].split("@");
							if (aInfo.length == 2) {
								aenc = Integer.parseInt(aInfo[1]);
							}
						}
						if (aenc == 0) {
							aenc = null;
						}

						scannedChannel = scannedChannelRepository
								.findBySourceFrequencyPolarizationStreamSidApid(
										sourceName, frequency, polarization,
										streamId, sid, apid);

						if (scannedChannel == null) {
							scannedChannel = new ScannedChannel();

							scannedChannel.setSourceName(sourceName);
							scannedChannel.setFrequency(frequency);
							scannedChannel.setPolarization(polarization);
							scannedChannel.setStreamIdNullable(streamId);
							scannedChannel.setSid(sid);
							scannedChannel.setApid(apid);
						}

						scannedChannel.setScannedName(scannedName);
						scannedChannel.setProviderName(providerName);
						scannedChannel.setDvbsGen(dvbsGen);
						scannedChannel.setSymbolRate(symbolRate);
						scannedChannel.setVenc(venc);
						scannedChannel.setVpid(vpid);
						scannedChannel.setPcr(pcr);
						scannedChannel.setTpid(tpid);
						scannedChannel.setCaid(caid);
						scannedChannel.setNid(nid);
						scannedChannel.setTid(tid);
						scannedChannel.setRid(rid);
						scannedChannel.setAenc(aenc);
						scannedChannel.setRefreshed(true);

						if (scannedChannel.getId() != null) {
							scannedChannelRepository.update(scannedChannel);
						} else {
							scannedChannelRepository.add(scannedChannel);
						}
					}

					if (!lastFrequency.equals(frequency)
							|| !lastPolarization.equals(polarization)
							|| !lastStreamId.equals(streamId)) {
						transponder = transponderRepository
								.findBySourceFrequencyPolarizationStream(
										sourceId, frequency, polarization,
										streamId);
						if (transponder != null) {
							transponderChanged = false;
							if (dvbsGen != null) {
								if (!dvbsGen.equals(transponder.getDvbsGen())) {
									transponder.setDvbsGen(dvbsGen);
									transponderChanged = true;
								}
							} else {
								if (transponder.getDvbsGen() != null) {
									transponder.setDvbsGen(dvbsGen);
									transponderChanged = true;
								}
							}
							if (symbolRate != null) {
								if (!symbolRate.equals(transponder
										.getSymbolRate())) {
									transponder.setSymbolRate(symbolRate);
									transponderChanged = true;
								}
							} else {
								if (transponder.getSymbolRate() != null) {
									transponder.setSymbolRate(symbolRate);
									transponderChanged = true;
								}
							}
							if (nid != null) {
								if (!nid.equals(transponder.getNid())) {
									transponder.setNid(nid);
									transponderChanged = true;
								}
							} else {
								if (transponder.getNid() != null) {
									transponder.setNid(nid);
									transponderChanged = true;
								}
							}
							if (tid != null) {
								if (!tid.equals(transponder.getTid())) {
									transponder.setTid(tid);
									transponderChanged = true;
								}
							} else {
								if (transponder.getTid() != null) {
									transponder.setTid(tid);
									transponderChanged = true;
								}
							}
							if (transponderChanged) {
								transponderRepository.update(transponder);
							}
						}
						lastFrequency = frequency;
						lastPolarization = polarization;
						if (lastPolarization == null) {
							lastPolarization = "";
						}
						lastStreamId = streamId;
						if (lastStreamId == null) {
							lastStreamId = 0;
						}
					}
				}

				scannedChannelRepository.deleteNotRefreshed(scanSourceName);

				forcedUpdateChannels = channelRepository.findAll(sourceId, -1,
						COMPARISON_CHANGED_MAIN_FORCED);

				for (Channel updatedChannel : forcedUpdateChannels) {
					transponder = transponderRepository.findById(updatedChannel
							.getTranspId());
					scannedChannel = scannedChannelRepository
							.findBySourceFrequencyPolarizationStreamSidApid(
									scanSourceName, transponder.getFrequency(),
									transponder.getPolarization(),
									transponder.getStreamId(),
									updatedChannel.getSid(),
									updatedChannel.getApid());

					updatedChannel.setPcr(scannedChannel.getPcr());
					updatedChannel.setTpid(scannedChannel.getTpid());
					updatedChannel.setRid(scannedChannel.getRid());
					updatedChannel.setProviderName(scannedChannel
							.getProviderName());
					channelRepository.update(updatedChannel);
				}

				forcedUpdateIgnoredChannels = ignoredChannelRepository.findAll(
						sourceId, -1, COMPARISON_CHANGED_IGNORED_FORCED);

				for (IgnoredChannel updatedIgnoredChannel : forcedUpdateIgnoredChannels) {
					transponder = transponderRepository
							.findById(updatedIgnoredChannel.getTranspId());
					scannedChannel = scannedChannelRepository
							.findBySourceFrequencyPolarizationStreamSidApid(
									scanSourceName, transponder.getFrequency(),
									transponder.getPolarization(),
									transponder.getStreamId(),
									updatedIgnoredChannel.getSid(),
									updatedIgnoredChannel.getApid());

					updatedIgnoredChannel.setProviderName(scannedChannel
							.getProviderName());
					ignoredChannelRepository.update(updatedIgnoredChannel);
				}
			}

			catch (Exception ex) {
				logger.log(Level.WARNING,
						"Scan line " + lineNo + ": " + ex.getMessage(), ex);

				result = "Error: line " + lineNo + ": " + ex.getMessage();
			}
		} else {
			logger.log(Level.WARNING, "Source '" + scanSourceName
					+ "' not found in the database.");

			result = "Error: Source '" + scanSourceName
					+ "' not found in the database.";
		}

		return result;
	}

	// Add information on processed scan into the list
	public void addScanProcessingReport(String scanFileName, String details) {
		scanProcessingReports.add(new String[] { scanFileName, details });
	}

	// Clear the list of scan processing information
	public void clearScanProcessingReports() {
		scanProcessingReports.clear();
	}

	// (Re)Fill in the channel list
	@PostConstruct
	public void retrieveAllChannels() {
		channels = scannedChannelRepository.findAll(filteredSourceId,
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

	public List<ScannedChannel> getChannels() {

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

	public List<ScannedChannel> getCheckedChannels() {

		return checkedChannels;
	}

	public List<String[]> getScanProcessingReports() {

		return scanProcessingReports;
	}

	public int getComparisonFilter() {

		return comparisonFilter;
	}

	public void setComparisonFilter(int comparisonFilter) {
		this.comparisonFilter = comparisonFilter;
	}

	public ScannedChannel getWorkingChannel() {

		return workingChannel;
	}

	public void setWorkingChannel(ScannedChannel workingChannel) {
		this.workingChannel = workingChannel;
	}

	public Source getWorkingChannelSource() {

		return workingChannelSource;
	}

	public void setWorkingChannelSource(Source workingChannelSource) {
		this.workingChannelSource = workingChannelSource;
	}

	public Transponder getWorkingChannelTransponder() {

		return workingChannelTransponder;
	}

	public void setWorkingChannelTransponder(
			Transponder workingChannelTransponder) {
		this.workingChannelTransponder = workingChannelTransponder;
	}

	public Channel getEditedChannel() {

		return editedChannel;
	}

	public void setEditedChannel(Channel editedChannel) {
		this.editedChannel = editedChannel;
	}

	public IgnoredChannel getEditedIgnoredChannel() {

		return editedIgnoredChannel;
	}

	public void setEditedIgnoredChannel(IgnoredChannel editedIgnoredChannel) {
		this.editedIgnoredChannel = editedIgnoredChannel;
	}

	public long getAddedChannelGroupId() {

		return addedChannelGroupId;
	}

	public void setAddedChannelGroupId(long addedChannelGroupId) {
		this.addedChannelGroupId = addedChannelGroupId;
	}

}

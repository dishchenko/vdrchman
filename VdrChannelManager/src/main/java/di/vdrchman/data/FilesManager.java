package di.vdrchman.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import di.vdrchman.model.Channel;
import di.vdrchman.model.Group;
import di.vdrchman.model.Source;
import di.vdrchman.model.Transponder;

@SessionScoped
@Named
public class FilesManager implements Serializable {

	private static final long serialVersionUID = 8655706926659652902L;

	@Inject
	private SourceRepository sourceRepository;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private ChannelRepository channelRepository;

	@Inject
	private GroupRepository groupRepository;

	// The list of scans (scanned channel file datas)
	private List<Scan> scans = new ArrayList<Scan>();

	// Defines if locked channels will be exported to channels.conf
	private boolean lockedChannelsExported = false;

	// Add uploaded scan to the list of scans
	public void addScan(String fileName, byte[] data) {
		Scan scan;

		scan = new Scan();

		scan.setFileName(fileName);
		scan.setData(new byte[data.length]);
		System.arraycopy(data, 0, scan.getData(), 0, data.length);

		scans.add(scan);
	}

	// Clear the list of scans
	public void clearScans() {
		scans.clear();
	}

	// Download the data from string 'content' to the browser as text file with
	// name 'fileName'
	public void download(String fileName, String content) throws IOException {
		FacesContext facesContext;
		HttpServletResponse response;
		BufferedInputStream bis;
		BufferedOutputStream bos;
		byte[] buffer;

		facesContext = FacesContext.getCurrentInstance();
		response = (HttpServletResponse) facesContext.getExternalContext()
				.getResponse();

		response.reset();
		response.setContentType("text/plain");
		response.setHeader("Content-disposition", "attachment; filename=\""
				+ fileName + "\"");

		bis = null;
		bos = null;

		try {
			bis = new BufferedInputStream(new ByteArrayInputStream(
					content.getBytes("UTF-8")));
			bos = new BufferedOutputStream(response.getOutputStream());

			buffer = new byte[2048];
			for (int length; (length = bis.read(buffer)) > 0;) {
				bos.write(buffer, 0, length);
			}
		} finally {
			if (bos != null) {
				bos.close();
			}
			if (bis != null) {
				bis.close();
			}

			facesContext.responseComplete();
		}
	}

	// Build a sources.conf data using current application user defined
	// sources
	public String buildSourcesConf() {
		StringBuilder sb;
		List<Source> sources;

		sb = new StringBuilder();

		sb.append("# Sources configuration for VDR\n"
				+ "#\n"
				+ "# Format:\n"
				+ "#\n"
				+ "# code  description\n"
				+ "#\n"
				+ "# S (satellite) xy.z (orbital position in degrees) E or W (east or west)\n"
				+ "# Note: only the first part is actually used by VDR. The description part\n"
				+ "# is for the \"human\" interface for clarity.\n"
				+ "#\n"
				+ "# '&' means same orbital position but different host company.\n"
				+ "# '/' means same (or very little deviation) orbital position & host.\n"
				+ "# A value in () means this satellite is still in it's test phase.\n"
				+ "#\n"
				+ "# Please contact kls@tvdr.de before assigning a new code\n"
				+ "# to a description, in order to keep them unique.\n" + "\n"
				+ "# Satellites\n" + "\n" + "\n");

		sources = sourceRepository.findAll();

		for (Source source : sources) {
			sb.append(source.getName()).append("    ")
					.append(source.getDescription()).append("\n");
		}

		return sb.toString();
	}

	// Build a diseqc.conf data using current application user defined
	// sources
	public String buildDiseqcConf() {
		StringBuilder sb;
		List<Source> sources;

		sb = new StringBuilder();

		sb.append("# DiSEqC configuration for VDR\n"
				+ "#\n"
				+ "# Format:\n"
				+ "#\n"
				+ "# satellite slof polarization lof command...\n"
				+ "#\n"
				+ "# satellite:      one of the 'S' codes defined in sources.conf\n"
				+ "# slof:           switch frequency of LNB; the first entry with\n"
				+ "#                 an slof greater than the actual transponder\n"
				+ "#                 frequency will be used\n"
				+ "# polarization:   V = vertical, H = horizontal, L = Left circular, R = Right circular\n"
				+ "# lof:            the local oscillator frequency to subtract from\n"
				+ "#                 the actual transponder frequency\n"
				+ "# command:\n"
				+ "#   t         tone off\n"
				+ "#   T         tone on\n"
				+ "#   v         voltage low (13V)\n"
				+ "#   V         voltage high (18V)\n"
				+ "#   A         mini A\n"
				+ "#   B         mini B\n"
				+ "#   Sn        Satellite channel routing code sequence for bank n follows\n"
				+ "#   Wnn       wait nn milliseconds (nn may be any positive integer number)\n"
				+ "#   [xx ...]  hex code sequence (max. 6)\n"
				+ "#\n"
				+ "# The 'command...' part is optional.\n"
				+ "#\n"
				+ "# A line containing space separated integer numbers, terminated with a ':',\n"
				+ "# defines that any following DiSEqC sequences apply only to the given list\n"
				+ "# of device numbers.\n" + "\n");

		sources = sourceRepository.findAll();

		for (Source source : sources) {
			sb.append('\n').append(source.getName()).append(' ')
					.append(source.getLoV()).append('\n');
			sb.append('\n').append(source.getName()).append(' ')
					.append(source.getHiV()).append('\n');
			sb.append('\n').append(source.getName()).append(' ')
					.append(source.getLoH()).append('\n');
			sb.append('\n').append(source.getName()).append(' ')
					.append(source.getHiH()).append('\n');
		}

		return sb.toString();
	}

	// Build a rotor.conf data using current application user defined
	// sources
	public String buildRotorConf() {
		StringBuilder sb;
		List<Source> sources;

		sb = new StringBuilder();

		sb.append("# Format:\n" + "# position source\n"
				+ "# position - number\n" + "# source - S<angle><E|W>\n" + "\n"
				+ "\n");

		sources = sourceRepository.findAll();

		for (Source source : sources) {
			if (source.getRotor() != null) {
				sb.append(source.getRotor()).append(' ')
						.append(source.getName()).append('\n');
			}
		}

		return sb.toString();
	}

	// Build a rotorng.conf data using current application user defined
	// sources
	public String buildRotorngConf() {
		StringBuilder sb;
		List<Source> sources;

		sb = new StringBuilder();

		sb.append("# Format:\n" + "# source position\n"
				+ "# source - S<angle><E|W>\n" + "# position - number\n" + "\n"
				+ "\n");

		sources = sourceRepository.findAll();

		for (Source source : sources) {
			if (source.getRotor() != null) {
				sb.append(source.getName()).append(' ')
						.append(source.getRotor()).append('\n');
			}
		}

		return sb.toString();
	}

	// Build frequencies data using current application user defined
	// source transponders
	public String buildFreq(Source source) {
		StringBuilder sb;
		List<Transponder> transponders;
		Integer streamId;

		sb = new StringBuilder();

		sb.append("# Format:\n"
				+ "# S frequency polarity symbol_rate [AUTO] [AUTO] [AUTO] [stream ID]\n"
				+ "# S - S1|S2\n" + "# frequency - kHz\n"
				+ "# polarity - H|V\n" + "# symbol_rate - sps\n"
				+ "# stream ID - if multistream\n" + "\n" + "\n");

		transponders = transponderRepository.findAll(source.getId());

		for (Transponder transponder : transponders) {
			if (transponder.getIgnored()) {
				sb.append("# ");
			}
			sb.append('S').append(transponder.getDvbsGen()).append(' ')
					.append(transponder.getFrequency()).append("000 ")
					.append(transponder.getPolarization()).append(' ')
					.append(transponder.getSymbolRate()).append("000");
			streamId = transponder.getStreamIdNullable();
			if (streamId != null) {
				sb.append(" AUTO AUTO AUTO ").append(streamId);
			}
			sb.append('\n');
		}

		return sb.toString();
	}

	// Build a channels.conf data using current application user defined
	// channels and groups
	public String buildChannelsConf() {
		StringBuilder sb;
		List<Source> sources;
		Map<Long, Source> sourceMap;
		List<Transponder> transponders;
		Map<Long, Transponder> transponderMap;
		int channelLineNo;
		List<Group> groups;
		List<Channel> channels;
		Transponder transponder;
		Source source;
		String lang;
		Integer streamId;
		String transponderParams;
		Integer vpid;
		String vInfo;
		Integer pcr;
		Integer venc;
		String aInfo;
		Integer aenc;
		Integer tpid;
		String caid;
		Integer nid;
		Integer tid;

		sb = new StringBuilder();

		sources = sourceRepository.findAll();
		sourceMap = new HashMap<Long, Source>();
		for (Source theSource : sources) {
			sourceMap.put(theSource.getId(), theSource);
		}

		transponders = transponderRepository.findAll(-1);
		transponderMap = new HashMap<Long, Transponder>();
		for (Transponder theTransponder : transponders) {
			transponderMap.put(theTransponder.getId(), theTransponder);
		}

		channelLineNo = 0;

		groups = groupRepository.findAll();

		for (Group group : groups) {
			if (!group.getIgnored()) {
				sb.append(":@").append(group.getStartChannelNo()).append(' ')
						.append(group.getDescription()).append('\n');

				channels = channelRepository.findAllInGroup(group.getId());

				for (Channel channel : channels) {
					if (!channel.getLocked() || lockedChannelsExported) {
						transponder = transponderMap.get(channel.getTranspId());
						source = sourceMap.get(transponder.getSourceId());

						lang = channel.getLang();
						if (lang != null) {
							lang = lang.concat(") ");
						} else {
							lang = "";
						}

						streamId = transponder.getStreamIdNullable();
						transponderParams = transponder.getPolarization() + "S"
								+ (transponder.getDvbsGen() - 1);
						if (streamId != null) {
							transponderParams = transponderParams.concat("X")
									.concat(streamId.toString());
						}

						vpid = channel.getVpid();
						if (vpid != null) {
							vInfo = vpid.toString();
							pcr = channel.getPcr();
							if (pcr != null) {
								vInfo = vInfo.concat("+")
										.concat(pcr.toString());
							}
							venc = channel.getVenc();
							if (venc != null) {
								vInfo = vInfo.concat("=").concat(
										venc.toString());
							}
						} else {
							vInfo = "0";
						}

						aInfo = channel.getApid().toString();
						aenc = channel.getAenc();
						if (aenc != null) {
							aInfo = aInfo.concat("=@").concat(aenc.toString());
						}

						tpid = channel.getTpid();
						if (tpid == null) {
							tpid = 0;
						}

						caid = channel.getCaid();
						if (caid == null) {
							caid = "0";
						}

						nid = transponder.getNid();
						if (nid == null) {
							nid = 0;
						}

						tid = transponder.getTid();
						if (tid == null) {
							tid = 0;
						}

						++channelLineNo;

						sb.append(lang).append(channel.getName()).append(':')
								.append(transponder.getFrequency()).append(':')
								.append(transponderParams).append(':')
								.append(source.getName()).append(':')
								.append(transponder.getSymbolRate())
								.append(':').append(vInfo).append(':')
								.append(aInfo).append(':').append(tpid)
								.append(':').append(caid).append(':')
								.append(channel.getSid()).append(':')
								.append(nid).append(':').append(tid)
								.append(':').append(channelLineNo).append('\n');
					}
				}
			}
		}

		return sb.toString();
	}

	public List<Scan> getScans() {

		return scans;
	}

	public boolean isLockedChannelsExported() {

		return lockedChannelsExported;
	}

	public void setLockedChannelsExported(boolean lockedChannelsExported) {
		this.lockedChannelsExported = lockedChannelsExported;
	}

}

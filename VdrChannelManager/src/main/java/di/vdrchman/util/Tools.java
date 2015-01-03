package di.vdrchman.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import di.vdrchman.data.Scan;
import di.vdrchman.model.Group;

@Named
@Singleton
public class Tools {

	// Comparison filter modes
	// 0 - no filtering
	public static final int COMPARISON_NONE = 0;
	// 1 - new channels
	public static final int COMPARISON_NEW = 1;
	// 2 - changed main list channels
	public static final int COMPARISON_CHANGED_MAIN = 2;
	// 3 - changed ignored list channels
	public static final int COMPARISON_CHANGED_IGNORED = 3;
	// 4 - not scanned channels
	public static final int COMPARISON_NOT_SCANNED = 4;
	// 5 - changed main list channels eligible for forced update
	public static final int COMPARISON_CHANGED_MAIN_FORCED = 5;
	// 6 - changed ignored channels eligible for forced update
	public static final int COMPARISON_CHANGED_IGNORED_FORCED = 6;

	// JSF EL accessible map of comparison constants
	public static final Map<String, Integer> comparisonConst = new HashMap<String, Integer>();

	// Sort modes
	// 0 - (in groups) by mainlist channel sequence number
	public static final int SORT_MAIN_LIST_SEQNO = 0;
	// 1 - by transponder seqno, TV/Radio, sid, apid
	public static final int SORT_TRANSPONDER_TVRADIO_SID_APID = 1;

	// JSF EL accessible map of sort constants
	public static final Map<String, Integer> sortConst = new HashMap<String, Integer>();

	// Initialize the above map etc.
	static {
		comparisonConst.put("NONE", COMPARISON_NONE);
		comparisonConst.put("NEW", COMPARISON_NEW);
		comparisonConst.put("CHANGED_MAIN", COMPARISON_CHANGED_MAIN);
		comparisonConst.put("CHANGED_IGNORED", COMPARISON_CHANGED_IGNORED);
		comparisonConst.put("NOT_SCANNED", COMPARISON_NOT_SCANNED);

		sortConst.put("MAIN_LIST_SEQNO", SORT_MAIN_LIST_SEQNO);
		sortConst.put("TRANSPONDER_TVRADIO_SID_APID",
				SORT_TRANSPONDER_TVRADIO_SID_APID);
	}

	// Build a string consisting of comma delimited group names
	public String buildGroupNamesString(List<Group> groups) {
		StringBuilder sb;

		sb = new StringBuilder();

		if (!groups.isEmpty()) {
			for (Group group : groups) {
				sb.append(group.getName()).append(", ");
			}

			sb.setLength(sb.length() - 2);
		} else {
			sb.append("--");
		}

		return sb.toString();
	}

	// Build a string consisting of comma delimited group names and descriptions
	public String buildGroupDescriptionsString(List<Group> groups) {
		StringBuilder sb;

		sb = new StringBuilder();

		if (!groups.isEmpty()) {
			for (Group group : groups) {
				sb.append(group.getName()).append(" (")
						.append(group.getDescription()).append("), ");
			}

			sb.setLength(sb.length() - 2);
		}

		return sb.toString();
	}

	// Convert numeric video encoding value to string representation
	public String decodeVenc(Integer venc) {
		String result;

		result = "";

		if (venc != null) {
			switch (venc) {
			case 1:
			case 2:
				result = "MPEG-2";
				break;
			case 27:
				result = "MPEG-4";
				break;
			default:
				result = venc.toString();
			}
		}

		return result;
	}

	// Convert numeric audio encoding value to string representation
	public String decodeAenc(Integer aenc) {
		String result;

		result = "";

		if (aenc != null) {
			switch (aenc) {
			case 3:
			case 4:
				result = "MPEG";
				break;
			case 15:
				result = "ADTS";
				break;
			case 17:
				result = "LATM";
				break;
			case 6:
			case 129:
				result = "AC3";
				break;
			default:
				result = aenc.toString();
			}
		}

		return result;
	}

	// Build a string consisting of comma delimited uploaded scan file names
	public String buildUploadedScanFileNamesString(List<Scan> scans) {
		StringBuilder sb;

		sb = new StringBuilder();

		if (!scans.isEmpty()) {
			for (Scan scan : scans) {
				sb.append(scan.getFileName()).append(", ");
			}

			sb.setLength(sb.length() - 2);
		}

		return sb.toString();
	}

	public Map<String, Integer> getComparisonConst() {

		return comparisonConst;
	}

	public Map<String, Integer> getSortConst() {

		return sortConst;
	}

}

package di.vdrchman.util;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import di.vdrchman.model.Group;

@Named
@Singleton
public class Tools {

	// Build a string consisting of comma delimited group names
	public String buildGroupNamesString(List<Group> groups) {
		StringBuilder sb;

		sb = new StringBuilder();

		if (!groups.isEmpty()) {
			for (Group group : groups) {
				sb.append(group.getName() + ", ");
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
				sb.append(group.getName() + " (" + group.getDescription()
						+ "), ");
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

}

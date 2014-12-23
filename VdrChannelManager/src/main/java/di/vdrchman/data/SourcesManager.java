package di.vdrchman.data;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import di.vdrchman.model.Source;

@SessionScoped
@Named
public class SourcesManager implements Serializable {

	private static final long serialVersionUID = 2540832650345734528L;

	@Inject
	private SourceRepository sourceRepository;

	// Source list for the current application user
	private List<Source> sources;

	// Number of table rows per page
	private final int rowsPerPage = 5;
	// Current table scroller page
	private int scrollerPage = 1;

	// Map of source IDs and checked checkboxes
	private Map<Long, Boolean> sourceCheckboxes = new HashMap<Long, Boolean>();
	// List of checked sources built on checkboxes map
	private List<Source> checkedSources = new ArrayList<Source>();

	// The source which the user is going to add/update
	private Source editedSource = new Source();

	// The "clipboard": the place to store the source taken by user
	private Source takenSource = null;

	// Fill in checkedSources list with sources corresponding to checkboxes
	// checked in the data table on the page
	public void collectCheckedSources() {
		clearCheckedSources();

		for (Source source : sources) {
			if (sourceCheckboxes.get(source.getId()) != null) {
				if (sourceCheckboxes.get(source.getId())) {
					checkedSources.add(source);
				}
			}
		}
	}

	// Clear the list of checked sources
	public void clearCheckedSources() {
		checkedSources.clear();
	}

	// Clear the map of source checkboxes
	public void clearSourceCheckboxes() {
		sourceCheckboxes.clear();
	}

	// Find and set the table scroller page to show the source given
	public void turnScrollerPage(Source source) {
		List<Source> sources;
		int i;

		sources = sourceRepository.findAll();
		i = 0;
		for (Source theSource : sources) {
			if (theSource.getId().equals(source.getId())) {
				scrollerPage = i / rowsPerPage + 1;
				break;
			}
			++i;
		}
	}

	// Set scroller page value to the last page if it is beyond now.
	public void adjustLastScrollerPage() {
		int maxPageNo;

		maxPageNo = (sources.size() - 1) / rowsPerPage + 1;
		if (scrollerPage > maxPageNo) {
			scrollerPage = maxPageNo;
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
			sb.append(source.getName() + "    " + source.getDescription()
					+ "\n");
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
			sb.append("\n");
			sb.append(source.getName() + " " + source.getLoV() + "\n");
			sb.append(source.getName() + " " + source.getHiV() + "\n");
			sb.append(source.getName() + " " + source.getLoH() + "\n");
			sb.append(source.getName() + " " + source.getHiH() + "\n");
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
				sb.append(source.getRotor() + " " + source.getName() + "\n");
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
				sb.append(source.getName() + " " + source.getRotor() + "\n");
			}
		}

		return sb.toString();
	}

	// (Re)Fill in the source list
	@PostConstruct
	public void retrieveAllSources() {
		sources = sourceRepository.findAll();
	}

	public List<Source> getSources() {

		return sources;
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

	public Map<Long, Boolean> getSourceCheckboxes() {

		return sourceCheckboxes;
	}

	public List<Source> getCheckedSources() {

		return checkedSources;
	}

	public Source getEditedSource() {

		return editedSource;
	}

	public void setEditedSource(Source editedSource) {
		this.editedSource = editedSource;
	}

	public Source getTakenSource() {

		return takenSource;
	}

	public void setTakenSource(Source takenSource) {
		this.takenSource = takenSource;
	}

}

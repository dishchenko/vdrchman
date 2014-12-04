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
		List<Source> sources;

		sources = sourceRepository.findAll();

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

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
import di.vdrchman.model.Transponder;

@SessionScoped
@Named
public class TranspondersManager implements Serializable {

	private static final long serialVersionUID = 2384916739086349690L;

	@Inject
	private SourceRepository sourceRepository;

	@Inject
	private TransponderRepository transponderRepository;

	// ID of the Source to filter Transponder list on.
	// No filtering if it's negative.
	private long filteredSourceId = -1;

	// (Filtered) Transponder list for the current application user
	private List<Transponder> transponders;

	// Number of table rows per page
	private final int rowsPerPage = 15;
	// Current table scroller page
	private int scrollerPage = 1;

	// Map of Transponder IDs and checked checkboxes
	private Map<Long, Boolean> transponderCheckboxes = new HashMap<Long, Boolean>();
	// List of checked Transponders built on checkboxes map
	private List<Transponder> checkedTransponders = new ArrayList<Transponder>();

	// The Transponder which the user is going to add/update
	private Transponder editedTransponder = new Transponder();

	// Sequence number of the edited Transponder
	private int editedTransponderSeqno;

	// The "clipboard": the place to store the Transponder copied by user
	private Transponder copiedTransponder = null;

	// Fill in checkedTransponders list with Transponders corresponding
	// to checkboxes checked in the data table on the page
	public void collectCheckedTransponders(long sourceId) {
		List<Transponder> transponders;

		transponders = transponderRepository.findAll(sourceId);

		clearCheckedTransponders();

		for (Transponder transponder : transponders) {
			if (transponderCheckboxes.get(transponder.getId()) != null) {
				if (transponderCheckboxes.get(transponder.getId())) {
					checkedTransponders.add(transponder);
				}
			}
		}
	}

	// Clear the list of checked Transponders
	public void clearCheckedTransponders() {
		checkedTransponders.clear();
	}

	// Clear the map of Transponder checkboxes
	public void clearTransponderCheckboxes() {
		transponderCheckboxes.clear();
	}

	// Find and set the table scroller page to show the Transponder given
	public void turnScrollerPage(Transponder transponder) {
		List<Transponder> transponders;
		int i;

		if (filteredSourceId >= 0) {
			transponders = transponderRepository.findAll(filteredSourceId);
			i = 0;
			for (Transponder theTransponder : transponders) {
				if (theTransponder.getId().equals(transponder.getId())) {
					scrollerPage = i / rowsPerPage + 1;
					break;
				}
				++i;
			}
		} else {
			scrollerPage = (transponderRepository.getSeqno(transponder) - 1)
					/ rowsPerPage + 1;
		}
	}

	// Calculate the sequence number for the Transponder to be placed on top
	// of the current Transponder list. If current Transponder list is not empty
	// then it is the sequence number of its top Transponder. Otherwise it is
	// calculated the way the Transponder to be placed right after
	// the last existing Transponder of previous Sources
	public int calculateOnTopSeqno() {
		int result;
		Source source;
		Integer maxSeqnoOnSource;

		if (transponders.isEmpty()) {
			result = 1;
			if (filteredSourceId >= 0) {
				source = sourceRepository.findPrevious(filteredSourceId);
				while (source != null) {
					maxSeqnoOnSource = transponderRepository
							.findMaxSeqno(source.getId());
					if (maxSeqnoOnSource != null) {
						result = maxSeqnoOnSource + 1;
						break;
					}
					source = sourceRepository.findPrevious(source.getId());
				}
			}
		} else {
			result = transponderRepository.getSeqno(transponders.get(0));
		}

		return result;
	}

	// (Re)Fill in the Transponder list
	@PostConstruct
	public void retrieveAllTransponders() {
		transponders = transponderRepository.findAll(filteredSourceId);
	}

	public long getFilteredSourceId() {

		return filteredSourceId;
	}

	public void setFilteredSourceId(long filteredSourceId) {
		this.filteredSourceId = filteredSourceId;
	}

	public List<Transponder> getTransponders() {

		return transponders;
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

	public Map<Long, Boolean> getTransponderCheckboxes() {

		return transponderCheckboxes;
	}

	public List<Transponder> getCheckedTransponders() {

		return checkedTransponders;
	}

	public Transponder getEditedTransponder() {

		return editedTransponder;
	}

	public void setEditedTransponder(Transponder editedTransponder) {
		this.editedTransponder = editedTransponder;
	}

	public int getEditedTransponderSeqno() {

		return editedTransponderSeqno;
	}

	public void setEditedTransponderSeqno(int editedTransponderSeqno) {
		this.editedTransponderSeqno = editedTransponderSeqno;
	}

	public Transponder getCopiedTransponder() {

		return copiedTransponder;
	}

	public void setCopiedTransponder(Transponder copiedTransponder) {
		this.copiedTransponder = copiedTransponder;
	}

}

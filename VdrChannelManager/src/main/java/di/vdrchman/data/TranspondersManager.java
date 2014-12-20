package di.vdrchman.data;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import di.vdrchman.event.ScannedChannelAction;
import di.vdrchman.event.SourceAction;
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

	// ID of the source to filter transponder list on.
	// No filtering if it's negative.
	private long filteredSourceId = -1;

	// (Filtered) Transponder list for the current application user
	private List<Transponder> transponders;

	// Indicates that transponders list refresh is suggested
	private boolean transpondersRefreshNeeded = false;

	// Number of table rows per page
	private final int rowsPerPage = 15;
	// Current table scroller page
	private int scrollerPage = 1;

	// Map of transponder IDs and checked checkboxes
	private Map<Long, Boolean> transponderCheckboxes = new HashMap<Long, Boolean>();
	// List of checked transponders built on checkboxes map
	private List<Transponder> checkedTransponders = new ArrayList<Transponder>();

	// The transponder which the user is going to add/update
	private Transponder editedTransponder = new Transponder();

	// Sequence number of the edited transponder
	private int editedTranspSeqno;

	// The "clipboard": the place to store the transponder taken by user
	private Transponder takenTransponder = null;

	// Fill in checkedTransponders list with transponders corresponding
	// to checkboxes checked in the data table on the page
	public void collectCheckedTransponders() {
		clearCheckedTransponders();

		for (Transponder transponder : transponders) {
			if (transponderCheckboxes.get(transponder.getId()) != null) {
				if (transponderCheckboxes.get(transponder.getId())) {
					checkedTransponders.add(transponder);
				}
			}
		}
	}

	// Clear the list of checked transponders
	public void clearCheckedTransponders() {
		checkedTransponders.clear();
	}

	// Clear the map of transponder checkboxes
	public void clearTransponderCheckboxes() {
		transponderCheckboxes.clear();
	}

	// Find and set the table scroller page to show the transponder given
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
			scrollerPage = (transponderRepository.findSeqno(transponder) - 1)
					/ rowsPerPage + 1;
		}
	}

	// Calculate the sequence number for the transponder to be placed on top
	// of the current transponder list. If current transponder list is not empty
	// then it is the sequence number of its top transponder. Otherwise it is
	// calculated the way the transponder to be placed right after
	// the last existing transponder of previous sources
	public int calculateOnTopSeqno() {
		int result;
		Source source;
		Integer maxTranspSeqnoWithinSource;

		if (transponders.isEmpty()) {
			result = 1;
			if (filteredSourceId >= 0) {
				source = sourceRepository.findPrevious(filteredSourceId);
				while (source != null) {
					maxTranspSeqnoWithinSource = transponderRepository
							.findMaxSeqno(source.getId());
					if (maxTranspSeqnoWithinSource != null) {
						result = maxTranspSeqnoWithinSource + 1;
						break;
					}
					source = sourceRepository.findPrevious(source.getId());
				}
			}
		} else {
			result = transponderRepository.findSeqno(transponders.get(0));
		}

		return result;
	}

	// Cleanup the TransponderManager's data on SourceAction if needed
	public void onSourceAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final SourceAction sourceAction) {
		long sourceId;

		if (sourceAction.getAction() == SourceAction.Action.DELETE) {
			sourceId = sourceAction.getSource().getId();

			if (sourceId == filteredSourceId) {
				filteredSourceId = -1;
			}
			if (filteredSourceId < 0) {
				transpondersRefreshNeeded = true;
			}

			if (takenTransponder != null) {
				if (sourceId == takenTransponder.getSourceId()) {
					takenTransponder = null;
				}
			}
		}
	}

	// Cleanup the TransponderManager's data on ScannedChannelAction if needed
	public void onScannedChannelAction(
			@Observes(notifyObserver = Reception.IF_EXISTS) final ScannedChannelAction scannedChannelAction) {
		if (scannedChannelAction.getAction() == ScannedChannelAction.Action.SCAN_PROCESSED) {
			transpondersRefreshNeeded = true;
		}
	}

	// Re(Fill) in the transponder list only if it is suggested. Also try to
	// turn the table scroller page to keep the last page top transponder shown
	public void refreshTranspondersIfNeeded() {
		Transponder lastPageTopTransponder;

		if (transpondersRefreshNeeded) {
			lastPageTopTransponder = null;
			if (!transponders.isEmpty()) {
				lastPageTopTransponder = transponders.get((scrollerPage - 1)
						* rowsPerPage);
			}

			retrieveAllTransponders();

			if (!transponders.isEmpty()) {
				if (lastPageTopTransponder != null) {
					if (transponderRepository.findById(lastPageTopTransponder
							.getId()) != null) {
						turnScrollerPage(lastPageTopTransponder);
					} else {
						scrollerPage = 1;
					}
				} else {
					scrollerPage = 1;
				}
			}

			transpondersRefreshNeeded = false;
		}
	}

	// (Re)Fill in the transponder list
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

	public int getEditedTranspSeqno() {

		return editedTranspSeqno;
	}

	public void setEditedTranspSeqno(int editedTranspSeqno) {
		this.editedTranspSeqno = editedTranspSeqno;
	}

	public Transponder getTakenTransponder() {

		return takenTransponder;
	}

	public void setTakenTransponder(Transponder takenTransponder) {
		this.takenTransponder = takenTransponder;
	}

}

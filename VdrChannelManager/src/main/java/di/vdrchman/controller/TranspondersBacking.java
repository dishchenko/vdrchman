package di.vdrchman.controller;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Model;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.TransponderRepository;
import di.vdrchman.data.TranspondersManager;
import di.vdrchman.event.TransponderAction;
import di.vdrchman.model.Transponder;

@Model
public class TranspondersBacking {

	@Inject
	private TranspondersManager transpondersManager;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private Event<TransponderAction> transponderActionEvent;

	// The user is going to add a new transponder on top of
	// the current transponder list
	public void intendAddTransponderOnTop() {
		Transponder transponder;

		transponder = new Transponder();
		transponder.setSourceId(transpondersManager.getFilteredSourceId());
		transpondersManager.setEditedTransponder(transponder);
		transpondersManager.setEditedTranspSeqno(transpondersManager
				.calculateOnTopSeqno());
	}

	// The user is going to add a new transponder and place it right after
	// the checked transponder in the list
	public void intendAddTransponderAfter() {
		Transponder transponder;

		transponder = new Transponder();
		transponder.setSourceId(transpondersManager.getFilteredSourceId());
		transpondersManager.collectCheckedTransponders();
		transpondersManager.setEditedTransponder(transponder);
		transpondersManager
				.setEditedTranspSeqno(transponderRepository
						.findSeqno(transpondersManager.getCheckedTransponders()
								.get(0)) + 1);
	}

	// Really adding a new transponder to the place specified
	public void doAddTransponder() {
		transponderRepository.add(transpondersManager.getEditedTransponder(),
				transpondersManager.getEditedTranspSeqno());
		transponderActionEvent.fire(new TransponderAction(transpondersManager
				.getEditedTransponder(), TransponderAction.Action.ADD));
		transpondersManager.retrieveAllTransponders();
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
		transpondersManager.turnScrollerPage(transpondersManager
				.getEditedTransponder());
	}

	// The user is going to update a transponder
	public void intendUpdateTransponder(Transponder transponder) {
		transpondersManager.setEditedTransponder(new Transponder(transponder));
	}

	// Really updating the transponder
	public void doUpdateTransponder() {
		transponderRepository
				.update(transpondersManager.getEditedTransponder());
		transponderActionEvent.fire(new TransponderAction(transpondersManager
				.getEditedTransponder(), TransponderAction.Action.UPDATE));
		transpondersManager.retrieveAllTransponders();
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
	}

	// Going to remove some checked transponders
	public void intendRemoveTransponders() {
		transpondersManager.collectCheckedTransponders();
	}

	// Do that removal
	public void doRemoveTransponders() {
		for (Transponder transponder : transpondersManager
				.getCheckedTransponders()) {
			transponderRepository.delete(transponder);
			transponderActionEvent.fire(new TransponderAction(transponder,
					TransponderAction.Action.DELETE));
		}
		transpondersManager.retrieveAllTransponders();
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
		transpondersManager.adjustLastScrollerPage();
	}

	// Let's take the checked transponder's data on the "clipboard"
	public void takeTransponder() {
		transpondersManager.collectCheckedTransponders();
		transpondersManager.setTakenTransponder(new Transponder(
				transpondersManager.getCheckedTransponders().get(0)));
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
	}

	// The user's gonna add a new transponder on top of the list
	// using data from the "clipboard"
	public void intendCopyTransponderOnTop() {
		transpondersManager.setEditedTransponder(new Transponder(
				transpondersManager.getTakenTransponder()));
		transpondersManager.getEditedTransponder().setId(null);
		transpondersManager.setEditedTranspSeqno(transpondersManager
				.calculateOnTopSeqno());
	}

	// The user is going to add a new transponder using data from the
	// "clipboard" and place it right after the checked transponder in the list
	public void intendCopyTransponderAfter() {
		transpondersManager.collectCheckedTransponders();
		transpondersManager.setEditedTransponder(new Transponder(
				transpondersManager.getTakenTransponder()));
		transpondersManager.getEditedTransponder().setId(null);
		transpondersManager
				.setEditedTranspSeqno(transponderRepository
						.findSeqno(transpondersManager.getCheckedTransponders()
								.get(0)) + 1);
	}

	// Remember the transponder taken on the "clipboard"?
	// Move the original one on top of the current list
	public void moveTransponderOnTop() {
		transponderRepository.move(transpondersManager.getTakenTransponder(),
				transpondersManager.calculateOnTopSeqno());
		transponderActionEvent.fire(new TransponderAction(transpondersManager
				.getTakenTransponder(), TransponderAction.Action.MOVE));
		transpondersManager.retrieveAllTransponders();
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
		transpondersManager.turnScrollerPage(transpondersManager
				.getTakenTransponder());
	}

	// Now move the transponder taken on the "clipboard" right after
	// the checked transponder in the list
	public void moveTransponderAfter() {
		int curSeqno;
		int newSeqno;

		transpondersManager.collectCheckedTransponders();
		curSeqno = transponderRepository.findSeqno(transpondersManager
				.getTakenTransponder());
		newSeqno = transponderRepository.findSeqno(transpondersManager
				.getCheckedTransponders().get(0));
		if (newSeqno < curSeqno) {
			++newSeqno;
		}
		transponderRepository.move(transpondersManager.getTakenTransponder(),
				newSeqno);
		transponderActionEvent.fire(new TransponderAction(transpondersManager
				.getTakenTransponder(), TransponderAction.Action.MOVE));
		transpondersManager.retrieveAllTransponders();
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
		transpondersManager.turnScrollerPage(transpondersManager
				.getTakenTransponder());
	}

	// On changing the source filter selection clear the "clipboard"
	// if a source is selected and it is not the one taken transponder
	// relates to. Also try to stay on the scroller page which includes
	// the previous shown page top transponder
	public void onSourceMenuSelection(ValueChangeEvent event) {
		Transponder lastPageTopTransponder;
		long filteredSourceId;
		Transponder takenTransponder;

		lastPageTopTransponder = null;
		if (!transpondersManager.getTransponders().isEmpty()) {
			lastPageTopTransponder = transpondersManager.getTransponders().get(
					(transpondersManager.getScrollerPage() - 1)
							* transpondersManager.getRowsPerPage());
		}
		filteredSourceId = (Long) event.getNewValue();
		if (filteredSourceId >= 0) {
			takenTransponder = transpondersManager.getTakenTransponder();
			if (takenTransponder != null) {
				if (filteredSourceId != takenTransponder.getSourceId()) {
					transpondersManager.setTakenTransponder(null);
				}
			}
		}
		transpondersManager.setFilteredSourceId(filteredSourceId);
		transpondersManager.retrieveAllTransponders();
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
		if (!transpondersManager.getTransponders().isEmpty()) {
			if (lastPageTopTransponder != null) {
				if (filteredSourceId == lastPageTopTransponder.getSourceId()) {
					transpondersManager
							.turnScrollerPage(lastPageTopTransponder);
				} else {
					if (filteredSourceId < 0) {
						transpondersManager
								.turnScrollerPage(lastPageTopTransponder);
					} else {
						transpondersManager
								.turnScrollerPage(transpondersManager
										.getTransponders().get(0));
					}
				}
			} else {
				transpondersManager.turnScrollerPage(transpondersManager
						.getTransponders().get(0));
			}
		}
	}

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
	}

}

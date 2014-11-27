package di.vdrchman.controller;

import javax.enterprise.inject.Model;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.TransponderRepository;
import di.vdrchman.data.TranspondersManager;
import di.vdrchman.model.Transponder;

@Model
public class TranspondersBacking {

	@Inject
	private TranspondersManager transpondersManager;

	@Inject
	private TransponderRepository transponderRepository;

	public void intendAddTransponderOnTop() {
		Transponder transponder;

		transponder = new Transponder();
		transponder.setSourceId(transpondersManager.getFilteredSourceId());
		transpondersManager.setEditedTransponder(transponder);
		transpondersManager.setEditedTransponderSeqno(transpondersManager
				.calculateOnTopSeqno());
	}

	public void intendAddTransponderAfter() {
		Transponder transponder;

		transponder = new Transponder();
		transponder.setSourceId(transpondersManager.getFilteredSourceId());
		transpondersManager.collectCheckedTransponders(transpondersManager
				.getFilteredSourceId());
		transpondersManager.setEditedTransponder(transponder);
		transpondersManager
				.setEditedTransponderSeqno(transponderRepository
						.getSeqno(transpondersManager.getCheckedTransponders()
								.get(0)) + 1);
	}

	public void doAddTransponder() {
		transponderRepository.add(transpondersManager.getEditedTransponder(),
				transpondersManager.getEditedTransponderSeqno());
		transpondersManager.retrieveAllTransponders();
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
		transpondersManager.turnScrollerPage(transpondersManager
				.getEditedTransponder());
	}

	public void intendUpdateTransponder(Transponder transponder) {
		transpondersManager.setEditedTransponder(new Transponder(transponder));
	}

	public void doUpdateTransponder() {
		transponderRepository
				.update(transpondersManager.getEditedTransponder());
		transpondersManager.retrieveAllTransponders();
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
	}

	public void intendRemoveTransponders() {
		transpondersManager.collectCheckedTransponders(transpondersManager
				.getFilteredSourceId());
	}

	public void doRemoveTransponders() {
		for (Transponder transponder : transpondersManager
				.getCheckedTransponders()) {
			transponderRepository.delete(transponder);
		}
		transpondersManager.retrieveAllTransponders();
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
	}

	public void copyTransponder() {
		transpondersManager.collectCheckedTransponders(transpondersManager
				.getFilteredSourceId());
		transpondersManager.setCopiedTransponder(new Transponder(
				transpondersManager.getCheckedTransponders().get(0)));
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
	}

	public void intendPasteTransponderOnTop() {
		transpondersManager.setEditedTransponder(new Transponder(
				transpondersManager.getCopiedTransponder()));
		transpondersManager.getEditedTransponder().setId(null);
		transpondersManager.setEditedTransponderSeqno(transpondersManager
				.calculateOnTopSeqno());
	}

	public void intendPasteTransponderAfter() {
		transpondersManager.collectCheckedTransponders(transpondersManager
				.getFilteredSourceId());
		transpondersManager.setEditedTransponder(new Transponder(
				transpondersManager.getCopiedTransponder()));
		transpondersManager.getEditedTransponder().setId(null);
		transpondersManager
				.setEditedTransponderSeqno(transponderRepository
						.getSeqno(transpondersManager.getCheckedTransponders()
								.get(0)) + 1);
	}

	public void moveTransponderOnTop() {
		transponderRepository.move(transpondersManager.getCopiedTransponder(),
				transpondersManager.calculateOnTopSeqno());
		transpondersManager.retrieveAllTransponders();
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
		transpondersManager.turnScrollerPage(transpondersManager
				.getCopiedTransponder());
	}

	public void moveTransponderAfter() {
		int curSeqno;
		int newSeqno;

		transpondersManager.collectCheckedTransponders(transpondersManager
				.getFilteredSourceId());
		curSeqno = transponderRepository.getSeqno(transpondersManager
				.getCopiedTransponder());
		newSeqno = transponderRepository.getSeqno(transpondersManager
				.getCheckedTransponders().get(0));
		if (newSeqno < curSeqno) {
			++newSeqno;
		}
		transponderRepository.move(transpondersManager.getCopiedTransponder(),
				newSeqno);
		transpondersManager.retrieveAllTransponders();
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
		transpondersManager.turnScrollerPage(transpondersManager
				.getCopiedTransponder());
	}

	public void onSourceMenuSelection(ValueChangeEvent event) {
		Transponder lastPageTopTransponder;
		long filteredSourceId;
		Transponder copiedTransponder;

		lastPageTopTransponder = null;
		if (!transpondersManager.getTransponders().isEmpty()) {
			lastPageTopTransponder = transpondersManager.getTransponders().get(
					(transpondersManager.getScrollerPage() - 1)
							* transpondersManager.getRowsPerPage());
		}
		filteredSourceId = (Long) event.getNewValue();
		if (filteredSourceId >= 0) {
			copiedTransponder = transpondersManager.getCopiedTransponder();
			if (copiedTransponder != null) {
				if (filteredSourceId != copiedTransponder.getSourceId()) {
					transpondersManager.setCopiedTransponder(null);
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

	public void onDataTableScroll(DataScrollEvent event) {
		transpondersManager.clearCheckedTransponders();
		transpondersManager.clearTransponderCheckboxes();
	}

}

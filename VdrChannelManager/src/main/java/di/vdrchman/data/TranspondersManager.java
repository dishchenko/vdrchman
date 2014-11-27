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

	private long filteredSourceId = -1;

	private List<Transponder> transponders;

	private final int rowsPerPage = 15;
	private int scrollerPage = 1;

	private Map<Long, Boolean> transponderCheckboxes = new HashMap<Long, Boolean>();
	private List<Transponder> checkedTransponders = new ArrayList<Transponder>();

	private Transponder editedTransponder = new Transponder();

	private int editedTransponderSeqno;

	private Transponder copiedTransponder = null;

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

	public void clearCheckedTransponders() {
		checkedTransponders.clear();
	}

	public void clearTransponderCheckboxes() {
		transponderCheckboxes.clear();
	}

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

	public int calculateOnTopSeqno() {
		int result;
		Source source;
		Integer maxSourceSeqno;

		if (transponders.isEmpty()) {
			result = 1;
			if (filteredSourceId >= 0) {
				source = sourceRepository.findPrevious(filteredSourceId);
				while (source != null) {
					maxSourceSeqno = transponderRepository.findMaxSeqno(source
							.getId());
					if (maxSourceSeqno != null) {
						result = maxSourceSeqno + 1;
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

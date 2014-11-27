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

	private List<Source> sources;

	private final int rowsPerPage = 5;
	private int scrollerPage = 1;

	private Map<Long, Boolean> sourceCheckboxes = new HashMap<Long, Boolean>();
	private List<Source> checkedSources = new ArrayList<Source>();

	private Source editedSource = new Source();

	private Source copiedSource = null;

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

	public void clearCheckedSources() {
		checkedSources.clear();
	}

	public void clearSourceCheckboxes() {
		sourceCheckboxes.clear();
	}

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

	public Source getCopiedSource() {
		return copiedSource;
	}

	public void setCopiedSource(Source copiedSource) {
		this.copiedSource = copiedSource;
	}

}

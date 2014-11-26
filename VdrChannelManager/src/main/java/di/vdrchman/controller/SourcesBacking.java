package di.vdrchman.controller;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.SourceRepository;
import di.vdrchman.data.SourcesManager;
import di.vdrchman.model.Source;

@Model
public class SourcesBacking {

	@Inject
	private SourcesManager sourcesManager;

	@Inject
	private SourceRepository sourceRepository;

	public void intendAddSource() {
		sourcesManager.setEditedSource(new Source());
	}

	public void doAddSource() {
		sourceRepository.add(sourcesManager.getEditedSource());
		sourcesManager.retrieveAllSources();
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
		sourcesManager.turnScrollerPage(sourcesManager.getEditedSource());
	}

	public void intendUpdateSource(Source source) {
		sourcesManager.setEditedSource(new Source(source));
	}

	public void doUpdateSource() {
		sourceRepository.update(sourcesManager.getEditedSource());
		sourcesManager.retrieveAllSources();
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
		sourcesManager.turnScrollerPage(sourcesManager.getEditedSource());
	}

	public void intendRemoveSources() {
		sourcesManager.collectCheckedSources();
	}

	public void doRemoveSources() {
		for (Source source : sourcesManager.getCheckedSources()) {
			sourceRepository.delete(source);
		}
		sourcesManager.retrieveAllSources();
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
	}

	public void copySource() {
		sourcesManager.collectCheckedSources();
		sourcesManager.setCopiedSource(new Source(sourcesManager
				.getCheckedSources().get(0)));
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
	}

	public void intendPasteSource() {
		sourcesManager.setEditedSource(new Source(sourcesManager
				.getCopiedSource()));
		sourcesManager.getEditedSource().setId(null);
	}

	public void onDataTableScroll(DataScrollEvent event) {
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
	}

}

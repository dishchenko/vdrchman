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

	// The user is going to add a new Source
	public void intendAddSource() {
		sourcesManager.setEditedSource(new Source());
	}

	// Really adding a new Source
	public void doAddSource() {
		sourceRepository.add(sourcesManager.getEditedSource());
		sourcesManager.retrieveAllSources();
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
		sourcesManager.turnScrollerPage(sourcesManager.getEditedSource());
	}

	// The user is going to update a Source
	public void intendUpdateSource(Source source) {
		sourcesManager.setEditedSource(new Source(source));
	}

	// Really updating the Source
	public void doUpdateSource() {
		sourceRepository.update(sourcesManager.getEditedSource());
		sourcesManager.retrieveAllSources();
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
		sourcesManager.turnScrollerPage(sourcesManager.getEditedSource());
	}

	// Going to remove some checked Sources
	public void intendRemoveSources() {
		sourcesManager.collectCheckedSources();
	}

	// Do that removal
	public void doRemoveSources() {
		for (Source source : sourcesManager.getCheckedSources()) {
			sourceRepository.delete(source);
		}
		sourcesManager.retrieveAllSources();
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
	}

	// Let's put the checked Source's data on the "clipboard"
	public void copySource() {
		sourcesManager.collectCheckedSources();
		sourcesManager.setCopiedSource(new Source(sourcesManager
				.getCheckedSources().get(0)));
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
	}

	// The user's gonna add a new Source using data from the "clipboard" 
	public void intendPasteSource() {
		sourcesManager.setEditedSource(new Source(sourcesManager
				.getCopiedSource()));
		sourcesManager.getEditedSource().setId(null);
	}

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
	}

}

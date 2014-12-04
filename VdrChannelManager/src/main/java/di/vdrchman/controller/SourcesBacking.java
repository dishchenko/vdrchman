package di.vdrchman.controller;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.SourceRepository;
import di.vdrchman.data.SourcesManager;
import di.vdrchman.event.SourceAction;
import di.vdrchman.model.Source;

@Model
public class SourcesBacking {

	@Inject
	private SourcesManager sourcesManager;

	@Inject
	private SourceRepository sourceRepository;

	@Inject
	private Event<SourceAction> sourceActionEvent;

	// The user is going to add a new source
	public void intendAddSource() {
		sourcesManager.setEditedSource(new Source());
	}

	// Really adding a new source
	public void doAddSource() {
		sourceRepository.add(sourcesManager.getEditedSource());
		sourceActionEvent.fire(new SourceAction(sourcesManager
				.getEditedSource(), SourceAction.Action.ADD));
		sourcesManager.retrieveAllSources();
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
		sourcesManager.turnScrollerPage(sourcesManager.getEditedSource());
	}

	// The user is going to update a source
	public void intendUpdateSource(Source source) {
		sourcesManager.setEditedSource(new Source(source));
	}

	// Really updating the source
	public void doUpdateSource() {
		sourceRepository.update(sourcesManager.getEditedSource());
		sourceActionEvent.fire(new SourceAction(sourcesManager
				.getEditedSource(), SourceAction.Action.UPDATE));
		sourcesManager.retrieveAllSources();
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
		sourcesManager.turnScrollerPage(sourcesManager.getEditedSource());
	}

	// Going to remove some checked sources
	public void intendRemoveSources() {
		sourcesManager.collectCheckedSources();
	}

	// Do that removal
	public void doRemoveSources() {
		for (Source source : sourcesManager.getCheckedSources()) {
			sourceRepository.delete(source);
			sourceActionEvent.fire(new SourceAction(source,
					SourceAction.Action.DELETE));
		}
		sourcesManager.retrieveAllSources();
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
	}

	// Let's take the checked source's data on the "clipboard"
	public void takeSource() {
		sourcesManager.collectCheckedSources();
		sourcesManager.setTakenSource(new Source(sourcesManager
				.getCheckedSources().get(0)));
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
	}

	// The user's gonna add a new source using data from the "clipboard"
	public void intendCopySource() {
		sourcesManager.setEditedSource(new Source(sourcesManager
				.getTakenSource()));
		sourcesManager.getEditedSource().setId(null);
	}

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		sourcesManager.clearCheckedSources();
		sourcesManager.clearSourceCheckboxes();
	}

}

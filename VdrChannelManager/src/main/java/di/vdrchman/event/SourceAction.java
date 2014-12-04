package di.vdrchman.event;

import di.vdrchman.model.Source;

public class SourceAction {

	public enum Action {
		ADD, UPDATE, DELETE
	};

	private Source source;
	private Action action;

	public SourceAction () {
		// do nothing
	}

	public SourceAction (Source source, Action action) {
		this.source = source;
		this.action = action;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

}

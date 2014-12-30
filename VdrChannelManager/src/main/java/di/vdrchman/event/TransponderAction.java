package di.vdrchman.event;

import di.vdrchman.model.Transponder;

public class TransponderAction {

	public enum Action {
		ADD, UPDATE, MOVE, DELETE
	};

	private Transponder transponder;
	private Action action;

	public TransponderAction () {
		// do nothing
	}

	public TransponderAction (Transponder transponder, Action action) {
		this.transponder = transponder;
		this.action = action;
	}

	public Transponder getTransponder() {

		return transponder;
	}

	public void setTransponder(Transponder transponder) {
		this.transponder = transponder;
	}

	public Action getAction() {

		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

}

package di.vdrchman.event;

public class ScannedChannelAction {

	public enum Action {
		SCAN_PROCESSED, CHANNEL_ADDED
	};

	private Action action;

	public ScannedChannelAction() {
		// do nothing
	}

	public ScannedChannelAction(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

}

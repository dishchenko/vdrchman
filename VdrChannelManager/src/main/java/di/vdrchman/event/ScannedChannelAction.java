package di.vdrchman.event;

public class ScannedChannelAction {

	public enum Action {
		SCAN_PROCESSED, CHANNEL_ADDED, CHANNEL_UPDATED, CHANNEL_REMOVED, IGNORED_CHANNEL_ADDED, IGNORED_CHANNEL_UPDATED, IGNORED_CHANNEL_REMOVED
	};

	private Action action;
	private long sourceId;
	private long transpId;

	public ScannedChannelAction() {
		// do nothing
	}

	public ScannedChannelAction(Action action, long sourceId, long transpId) {
		this.action = action;
		this.sourceId = sourceId;
		this.transpId = transpId;
	}

	public Action getAction() {

		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public long getSourceId() {

		return sourceId;
	}

	public void setSourceId(long sourceId) {
		this.sourceId = sourceId;
	}

	public long getTranspId() {

		return transpId;
	}

	public void setTranspId(long transpId) {
		this.transpId = transpId;
	}

}

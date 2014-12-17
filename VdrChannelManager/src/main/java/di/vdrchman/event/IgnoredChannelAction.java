package di.vdrchman.event;

import di.vdrchman.model.IgnoredChannel;

public class IgnoredChannelAction {

	public enum Action {
		ADD, UPDATE, DELETE
	};

	private IgnoredChannel channel;
	private Action action;

	public IgnoredChannelAction() {
		// do nothing
	}

	public IgnoredChannelAction(IgnoredChannel channel, Action action) {
		this.channel = channel;
		this.action = action;
	}

	public IgnoredChannel getChannel() {
		return channel;
	}

	public void setChannel(IgnoredChannel channel) {
		this.channel = channel;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

}

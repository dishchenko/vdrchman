package di.vdrchman.event;

import di.vdrchman.model.Channel;

public class ChannelAction {

	public enum Action {
		ADD, UPDATE, DELETE, UPDATE_GROUPS
	};

	private Channel channel;
	private Action action;

	public ChannelAction() {
		// do nothing
	}

	public ChannelAction(Channel channel, Action action) {
		this.channel = channel;
		this.action = action;
	}

	public Channel getChannel() {

		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public Action getAction() {

		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

}

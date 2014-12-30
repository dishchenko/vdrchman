package di.vdrchman.event;

public class SeqnoRenumberRequest {

	public enum Target {
		TRANSPONDERS, CHANNELS, CHANNEL_GROUPS
	}

	private Target target;

	public SeqnoRenumberRequest() {
		// do nothing
	}

	public SeqnoRenumberRequest(Target target) {
		this.target = target;
	}

	public Target getTarget() {

		return target;
	}

	public void setTarget(Target target) {
		this.target = target;
	}

}

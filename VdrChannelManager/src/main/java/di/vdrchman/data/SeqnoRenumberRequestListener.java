package di.vdrchman.data;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;

import di.vdrchman.event.SeqnoRenumberRequest;

@SessionScoped
public class SeqnoRenumberRequestListener implements Serializable {

	private static final long serialVersionUID = 5567935751553695031L;

	@Inject
	private TransponderRepository transponderRepository;

	@Inject
	private ChannelRepository channelRepository;

	// Renumber transponders' and/or channels' sequence numbers on request event
	public void onSeqnoRenumberRequest(
			@Observes(notifyObserver = Reception.ALWAYS) final SeqnoRenumberRequest seqnoRenumberRequest) {
		if (seqnoRenumberRequest.getTarget() == SeqnoRenumberRequest.Target.TRANSPONDERS) {
			transponderRepository.renumberSeqnos();
		}

		if (seqnoRenumberRequest.getTarget() == SeqnoRenumberRequest.Target.CHANNELS) {
			channelRepository.renumberSeqnos();
		}

		if (seqnoRenumberRequest.getTarget() == SeqnoRenumberRequest.Target.CHANNEL_GROUPS) {
			channelRepository.renumberSeqnosInGroups();
		}
	}

}

package di.vdrchman.data;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import di.vdrchman.model.Channel;

@Stateless
@Named
public class ChannelRepository {

	@Inject
	private EntityManager em;

	@Inject
	private User user;

	/**
	 * Builds a full or partial list of Channels belonging to
	 * the current application user depending on values of sourceId and
	 * transpId parameters.
	 * Channels are added to the list in ascending sequence number order.
	 * 
	 * @param sourceId the ID of the Source which Channels are added to
	 *                 the list, if both sourceId and transpId are
	 *                 negative then all current user's Channels are added
	 * @param transpId the ID of the Transponder which Channels are added to
	 *                 the list, if transpId is negative then sourceId
	 *                 value is taken into account
	 * @return the list of Channels found for the current application user
	 *         and given Source and Transponder IDs
	 */
	public List<Channel> findAll(long sourceId, long transpId) {
		List<Channel> result;
		Query query;
		List<?> queryResult;

		if (transpId < 0) {
			if (sourceId < 0) {
				query = em
						.createQuery("select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId order by cs.seqno");
				query.setParameter("userId", user.getId());
			} else {
				query = em
						.createQuery("select c, cs.seqno from Channel c, ChannelSeqno cs, Transponder t where c.id = cs.channelId and c.transpId = t.id and cs.userId = :userId and t.sourceId = :sourceId order by cs.seqno");
				query.setParameter("userId", user.getId());
				query.setParameter("sourceId", sourceId);
			}
		} else {
			query = em
					.createQuery("select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId and c.transpId = :transpId order by cs.seqno");
			query.setParameter("userId", user.getId());
			query.setParameter("transpId", transpId);
		}

		queryResult = query.getResultList();

		result = new ArrayList<Channel>();

		for (Object row : queryResult) {
			result.add((Channel) ((Object[]) row)[0]);
		}

		return result;
	}

}

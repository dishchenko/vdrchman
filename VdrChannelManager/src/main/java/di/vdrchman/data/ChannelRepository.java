package di.vdrchman.data;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import di.vdrchman.model.Channel;
import di.vdrchman.model.ChannelSeqno;
import di.vdrchman.model.Group;

@Stateless
@Named
public class ChannelRepository {

	@Inject
	private EntityManager em;

	@Inject
	private User user;

	/**
	 * Builds a full or partial list of Channels belonging to the current
	 * application user depending on values of sourceId and transpId parameters.
	 * Channels are added to the list in ascending sequence number order.
	 * 
	 * @param sourceId
	 *            the ID of the Source which Channels are added to the list, if
	 *            both sourceId and transpId are negative then all current
	 *            user's Channels are added
	 * @param transpId
	 *            the ID of the Transponder which Channels are added to the
	 *            list, if transpId is negative then sourceId value is taken
	 *            into account
	 * @return the list of Channels found for the current application user and
	 *         given Source and Transponder IDs
	 */
	public List<Channel> findAll(long sourceId, long transpId) {
		List<Channel> result;
		TypedQuery<Object[]> query;
		List<Object[]> queryResult;

		if (transpId < 0) {
			if (sourceId < 0) {
				query = em
						.createQuery(
								"select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId order by cs.seqno",
								Object[].class);
				query.setParameter("userId", user.getId());
			} else {
				query = em
						.createQuery(
								"select c, cs.seqno from Channel c, ChannelSeqno cs, Transponder t where c.id = cs.channelId and c.transpId = t.id and cs.userId = :userId and t.sourceId = :sourceId order by cs.seqno",
								Object[].class);
				query.setParameter("userId", user.getId());
				query.setParameter("sourceId", sourceId);
			}
		} else {
			query = em
					.createQuery(
							"select c, cs.seqno from Channel c, ChannelSeqno cs where c.id = cs.channelId and cs.userId = :userId and c.transpId = :transpId order by cs.seqno",
							Object[].class);
			query.setParameter("userId", user.getId());
			query.setParameter("transpId", transpId);
		}

		queryResult = query.getResultList();

		result = new ArrayList<Channel>();

		for (Object[] row : queryResult) {
			result.add((Channel) row[0]);
		}

		return result;
	}

	/**
	 * Finds maximum sequence number of Channel related to the Transponder with
	 * ID given.
	 * 
	 * @param transpId
	 *            the Transponder ID to find a Channel's maximum sequence number
	 * @return the maximum Channel sequence number or null if no Channels found
	 */
	public Integer findMaxSeqno(long transpId) {
		TypedQuery<Integer> query;

		if (transpId < 0) {
			query = em
					.createQuery(
							"select max(cs.seqno) from ChannelSeqno cs, Channel c where c.id = cs.channelId and cs.userId = :userId",
							Integer.class);
			query.setParameter("userId", user.getId());
		} else {
			query = em
					.createQuery(
							"select max(cs.seqno) from ChannelSeqno cs, Channel c where c.id = cs.channelId and cs.userId = :userId and c.transpId = :transpId",
							Integer.class);
			query.setParameter("userId", user.getId());
			query.setParameter("transpId", transpId);
		}

		return query.getSingleResult();
	}

	/**
	 * Returns a sequence number of the Channel given.
	 * 
	 * @param channel
	 *            the Channel to get the sequence number for
	 * @return the sequence number or null if no Channel found
	 */
	public Integer getSeqno(Channel channel) {
		Integer result;
		ChannelSeqno channelSeqno;

		result = null;

		channelSeqno = em.find(ChannelSeqno.class, channel.getId());
		if (channelSeqno != null) {
			result = channelSeqno.getSeqno();
		}

		return result;
	}

	/**
	 * Builds a list of groups in which the Channel with given ID is present.
	 * 
	 * @param channelId
	 *            the ID of the Channel to build the list of Groups for
	 * @return the list of Groups in which the Channel is present
	 */
	public List<Group> findGroups(long channelId) {
		TypedQuery<Group> query;

		query = em
				.createQuery(
						"select g from Group g where g.id in (select cg.groupId from ChannelGroup cg where cg.channelId = :channelId) order by g.description",
						Group.class);
		query.setParameter("channelId", channelId);

		return query.getResultList();
	}

}
